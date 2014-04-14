package models

import reactivemongo.bson.{BSONDocumentReader, BSONDocument, BSONDocumentWriter, BSONObjectID}
import models.LoginInfo.LoginInfoBSONWriter

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.modules.reactivemongo.json.BSONFormats._

case class User(name: String,
                emails: Seq[String],
                logins: Seq[LoginInfo],
                _id: Option[BSONObjectID])

case class LoginInfo(name: String,
                     token: String,
                     userId: String)

object LoginInfo {

  implicit object LoginInfoBSONWriter extends BSONDocumentWriter[LoginInfo] {
    def write(t: LoginInfo): BSONDocument = {
      BSONDocument(
        "name" -> t.name,
        "token" -> t.token,
        "userId" -> t.userId
      )
    }
  }

  implicit object LoginInfoBSONReader extends BSONDocumentReader[LoginInfo] {
    def read(bson: BSONDocument): LoginInfo = {
      new LoginInfo(
        bson.getAs[String]("name").get,
        bson.getAs[String]("token").get,
        bson.getAs[String]("userId").get
      )
    }
  }

  implicit val loginInfoJsonReads: Reads[LoginInfo] = (
    (JsPath \ "name").read[String] and
    (JsPath \ "token").read[String] and
    (JsPath \ "userId").read[String]
    )(LoginInfo.apply _)

  implicit val loginInfoJsonWrites: Writes[LoginInfo] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "token").write[String] and
    (JsPath \ "userId").write[String]
    )(unlift(LoginInfo.unapply))

  implicit val loginInfoFormats = Format(loginInfoJsonReads, loginInfoJsonWrites)
}

object User {

  implicit object UserBSONWriter extends BSONDocumentWriter[User] {
    def write(t: User): BSONDocument = {
      BSONDocument(
        "name" -> t.name,
        "emails" -> t.emails,
        "logins" -> t.logins
      )
    }
  }

  implicit object UserBSONReader extends BSONDocumentReader[User] {

    def read(bson: BSONDocument): User = {
      new User(
        bson.getAs[String]("name").get,
        bson.getAs[Seq[String]]("emails").get,
        bson.getAs[Seq[LoginInfo]]("logins").get,
        bson.getAs[BSONObjectID]("_id")
      )
    }
  }

  implicit val userJsonReads: Reads[User] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "emails").read[Seq[String]] and
      (JsPath \ "logins").read[Seq[LoginInfo]] and
      (JsPath \ "_id").readNullable[BSONObjectID]
    )(User.apply _)

  implicit val userJsonWrites: Writes[User] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "emails").write[Seq[String]] and
    (JsPath \ "logins").write[Seq[LoginInfo]] and
    (JsPath \ "id").writeNullable[BSONObjectID]
    )(unlift(User.unapply))

  implicit val userFormats = Format(userJsonReads, userJsonWrites)

}