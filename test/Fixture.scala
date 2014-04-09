
import org.apache.commons.io.IOUtils
import org.specs2.matcher.MatchResult
import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.MongoDriver

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global

trait Fixture[A, B, C] {

  def data: A

  def load(): Future[B]
  def destroy(): Future[C]

}

object Fixture {

  def usingFixture[A](fixture: Fixture[A, _, _])(functor: (A) => MatchResult[_]):MatchResult[_] = {
    Await.result(fixture.load(), 100 millis)
    try{
      functor(fixture.data)
    } finally {
      Await.result(fixture.destroy(), 100 millis)
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

  def data: JsValue = Json.parse(IOUtils.toString(stream))

  def load = {
    data match {
      case obj: JsObject => mongoCollection.insert(obj)
      case arr: JsArray => Future.sequence(arr.as[Seq[JsObject]].map {mongoCollection.insert(_)})
      case _ => throw new Exception("Unexpected JSON type")
    }
  }

  def destroy: Future[Boolean] = mongoCollection.drop()
}

object JsonFixture {

  def testCollection(collectionName: String): JSONCollection = {
    new MongoDriver()
      .connection(List("localhost"))
      .db("daedalus_test")
      .collection(collectionName)
  }

}