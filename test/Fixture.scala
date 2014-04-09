
import org.apache.commons.io.IOUtils
import org.specs2.matcher.MatchResult
import play.api.libs.json._
import play.api.Logger
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.MongoDriver

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scala.util.Try

trait Fixture[A, B, C] {

  def data: A

  def load(): Future[B]
  def destroy(): Future[C]

}

object Fixture {

  def usingFixture[A](fixture: Fixture[A, _, _])(functor: (A) => MatchResult[_]):MatchResult[_] = {
    Await.result(fixture.load(), 5000 millis)
    try{
      functor(fixture.data)
    } finally {
      Await.result(fixture.destroy(), 5000 millis)
    }
  }

}

abstract class FromStreamFixture[A, B, C](path: String) extends Fixture[A, B, C]{
  val stream = getClass.getResourceAsStream(s"/${path}")
}

class StringFixture(path:String) extends FromStreamFixture[String, Unit, Unit](path) {

  def data: String = IOUtils.toString(stream)

  def load() = future {

  }

  def destroy() = future {

  }

}

class JsonFixture(path: String, collection: String) extends FromStreamFixture[JsValue, AnyRef, Boolean](path) {

  val mongoCollection = JsonFixture.testCollection(collection)

  def data: JsValue = {
    val stringified = IOUtils.toString(stream)
    Logger.info(stringified)
    Json.parse(stringified)
  }

  def load = {
    data match {
      case obj: JsObject => {
        obj.value.get("objects").map { objs:JsValue =>
          Future.sequence(objs.as[Seq[JsObject]].map { mongoCollection.insert(_) })
        } getOrElse {
          mongoCollection.insert(obj)
        }
      }
      case _ => throw new Exception("Unexpected JSON type")
    }
  }

  def destroy: Future[Boolean] = mongoCollection.drop()
}

object JsonFixture {
  def removeCollection(s: String) = Try(Await.result(testCollection(s).drop(), 5000 milli))


  def testCollection(collectionName: String): JSONCollection = {
    new MongoDriver()
      .connection(List("localhost"))
      .db("daedalus_test")
      .collection(collectionName)
  }

}