package search

import reactivemongo.bson.BSONString
import org.apache.commons.io.IOUtils

object BSONCode {

  def apply(path: String): BSONString = {
    val stringValue = IOUtils.toString(getClass.getResourceAsStream(s"js/${path}"))
    BSONString(stringValue)
  }

}