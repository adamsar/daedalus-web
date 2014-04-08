package forms

import play.api.data.format.Formatter
import play.api.data.{Forms, Mapping, FormError}
import scala.util.Try
import models.{UnknownType, EntityType}
import models.EntityType._


object DaedalusMappings {

  implicit val commaDelimitedListFormatter = new Formatter[List[String]] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], List[String]] = {
      data.get(key).map { value =>
        Try(value.split(",").toList).map(Right(_)).getOrElse(
          error(key, s"${value} is not a comma delimited list")
        )
      }.getOrElse { error(key, "No string provided") }
    }

    private def error(key: String, msg:String) = Left(Seq(new FormError(key, msg)))

    override def unbind(key: String, value: List[String]): Map[String, String] = {
      Map(key -> value.mkString(","))
    }

  }

  def commaDelimitedList: Mapping[List[String]] = Forms.of[List[String]]

  implicit val entityTypeFormatter = new Formatter[EntityType] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], EntityType] = {
      data.get(key).map { value =>
        stringToType(value) match {
          case entityType:EntityType if(entityType.isInstanceOf[UnknownType]) => {
            Left(Seq(new FormError(key, s"${value} is not a known type")))
          }
          case entityType:EntityType => Right(entityType)
        }
      } getOrElse { Left(Seq(new FormError(key, "No entity type provided"))) }
    }

    def unbind(key: String, value: EntityType): Map[String, String] = {
      Map(key -> value.name)
    }

  }

  def entityType: Mapping[EntityType] = Forms.of[EntityType]

}
