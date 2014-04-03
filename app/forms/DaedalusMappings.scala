package forms

import play.api.data.format.Formatter
import play.api.data.{Forms, Mapping, FormError}
import scala.util.Try

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

}
