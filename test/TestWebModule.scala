import com.ning.http.client.{FluentCaseInsensitiveStringsMap, Cookie, Response}
import java.io.{ByteArrayInputStream, InputStream}
import java.net.URI
import java.nio.ByteBuffer
import java.util
import org.scalamock.scalatest.MockFactory
import org.scalatest.FunSuite
import org.specs2.matcher.MatchResult
import play.api.Logger
import request.TaskServerRequests
import scala.concurrent._
import scaldi.Module
import scala.collection.JavaConversions._
import ExecutionContext.Implicits.global

class MockResponse(response:String, uri:String, code:Int = 200) extends Response{
  def getStatusCode: Int = code

  def getStatusText: String = response

  def getResponseBodyAsBytes: Array[Byte] = response.getBytes

  def getResponseBodyAsByteBuffer: ByteBuffer = ByteBuffer.allocate(getResponseBodyAsBytes.length)

  def getResponseBodyAsStream: InputStream = new ByteArrayInputStream(getResponseBodyAsBytes)

  def getResponseBodyExcerpt(p1: Int, p2: String): String = response

  def getResponseBody(p1: String): String = response

  def getResponseBodyExcerpt(p1: Int): String = response

  def getResponseBody: String = response

  def getUri: URI = new URI(uri)

  def getContentType: String = "application/html"

  def getHeader(p1: String): String = ""

  def getHeaders(p1: String): util.List[String] = List[String]()

  def getHeaders: FluentCaseInsensitiveStringsMap = new FluentCaseInsensitiveStringsMap()

  def isRedirected: Boolean = false

  def getCookies: util.List[Cookie] = List[Cookie]()

  def hasResponseStatus: Boolean = true

  def hasResponseHeaders: Boolean = true

  def hasResponseBody: Boolean = true
}

class TestHttpRequests extends FunSuite with TaskServerRequests with MockFactory{

  def entitiesTask(text: String): dispatch.Future[Response] = future {
    Logger.info(TestHttpRequests.results)
    new MockResponse(TestHttpRequests.results, text, TestHttpRequests.code)
  }

}

object TestHttpRequests {

  var results:String = ""
  var code:Int = 200

  def withResults(response: String, newCode:Int = 200)(functor: () => MatchResult[_]) = {
    val oldResponse = results
    val oldCode = code
    results = response
    code = newCode
    val returnable = functor()
    results = oldResponse
    code = oldCode
    returnable
  }

}

class TestWebModule extends Module {
  bind[TaskServerRequests] to new TestHttpRequests
}
