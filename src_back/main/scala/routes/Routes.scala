package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import akka.actor.typed.ActorRef
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import actors.UserActor._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Success, Failure}

// Définition du format JSON pour les requêtes d'inscription et d'authentification
final case class RegisterUserJson(nom: String, email: String, password: String)
final case class AuthenticateUserJson(email: String, password: String)

// Sérialisation JSON
trait JsonFormats extends DefaultJsonProtocol {
  implicit val registerUserFormat: RootJsonFormat[RegisterUserJson] = jsonFormat3(RegisterUserJson)
  implicit val authenticateUserFormat: RootJsonFormat[AuthenticateUserJson] = jsonFormat2(AuthenticateUserJson)
  implicit val authResponseFormat: RootJsonFormat[AuthResponse] = jsonFormat2(AuthResponse)
  implicit val authValidationFormat: RootJsonFormat[AuthValidationResponse] = jsonFormat1(AuthValidationResponse)
}

class Routes(userActor: ActorRef[Command])(implicit system: ActorSystem[_]) extends JsonFormats {

  implicit val ec: scala.concurrent.ExecutionContextExecutor = system.executionContext
  implicit val timeout: Timeout = 5.seconds

  val route =
    pathPrefix("auth") {
      concat(
        path("register") {
          post {
            entity(as[RegisterUserJson]) { user =>
              val registerFuture: Future[AuthResponse] =
                userActor.ask(ref => RegisterUser(user.nom, user.email, user.password, ref))(timeout, system.scheduler)

              onComplete(registerFuture) {
                case Success(AuthResponse(Some(userId), Some(token))) =>
                  complete(StatusCodes.OK, s"""{"userId": $userId, "token": "$token"}""")
                case _ =>
                  complete(StatusCodes.Conflict, "User already exists")
              }
            }
          }
        },
        path("login") {
          post {
            entity(as[AuthenticateUserJson]) { credentials =>
              val authFuture: Future[AuthResponse] =
                userActor.ask(ref => AuthenticateUser(credentials.email, credentials.password, ref))(timeout, system.scheduler)

              onComplete(authFuture) {
                case Success(AuthResponse(Some(userId), Some(token))) =>
                  complete(StatusCodes.OK, s"""{"userId": $userId, "token": "$token"}""")
                case _ =>
                  complete(StatusCodes.Unauthorized, "Invalid credentials")
              }
            }
          }
        },
        path("validate") {
          get {
            parameter("token") { token =>
              val validateFuture: Future[AuthValidationResponse] =
                userActor.ask(ref => ValidateToken(token, ref))(timeout, system.scheduler)

              onComplete(validateFuture) {
                case Success(AuthValidationResponse(true)) => complete(StatusCodes.OK, "Token is valid")
                case Success(AuthValidationResponse(false)) => complete(StatusCodes.Unauthorized, "Invalid token")
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Erreur serveur: ${ex.getMessage}")
              }
            }
          }
        }
      )
    }
}
