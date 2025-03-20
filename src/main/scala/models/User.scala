package models

import spray.json.{DefaultJsonProtocol, JsString, JsValue, RootJsonFormat}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class User(
  id: Option[Int],            
  nom: String,                
  email: String,
  password: String,
  date_creation: Option[LocalDateTime] = None 
)

object UserJsonProtocol extends DefaultJsonProtocol {
  import spray.json._

  // Formatter pour LocalDateTime
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

  case class RegisterUserJson(nom: String, email: String, password: String)
  case class AuthenticateUserJson(email: String, password: String)

  implicit val registerUserJsonFormat: RootJsonFormat[RegisterUserJson] = jsonFormat3(RegisterUserJson)
  implicit val authenticateUserJsonFormat: RootJsonFormat[AuthenticateUserJson] = jsonFormat2(AuthenticateUserJson)

  implicit val localDateTimeFormat: RootJsonFormat[LocalDateTime] = new RootJsonFormat[LocalDateTime] {
    def write(date: LocalDateTime): JsValue = JsString(date.format(dateFormatter))
    def read(json: JsValue): LocalDateTime = json match {
      case JsString(str) => LocalDateTime.parse(str, dateFormatter)
      case _ => throw new DeserializationException("Invalid date format")
    }
  }

  // Format JSON pour User
  implicit val userFormat: RootJsonFormat[User] = jsonFormat5(User)
}
  