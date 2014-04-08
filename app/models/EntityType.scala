package models

case class EntityType(name: String)

class UnknownType(_name: String) extends EntityType(_name)

object EntityType {

  final val validTypes = Seq("dependency", "tag", "language")

  implicit def stringToType(typeString: String): EntityType = typeString match {
    case _type:String if(validTypes.contains(_type)) => new EntityType(_type)
    case _type:String => new UnknownType(_type)
  }

}
