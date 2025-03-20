package actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import services.AuthService
import scala.util.{Success, Failure}

// Définition des commandes
object UserActor {
  sealed trait Command
  final case class RegisterUser(nom: String, email: String, password: String, replyTo: ActorRef[AuthResponse]) extends Command
  final case class AuthenticateUser(email: String, password: String, replyTo: ActorRef[AuthResponse]) extends Command
  final case class ValidateToken(token: String, replyTo: ActorRef[AuthValidationResponse]) extends Command

  // Définition des réponses
  sealed trait Response
  final case class AuthResponse(userId: Option[Int], token: Option[String]) extends Response
  final case class AuthValidationResponse(isValid: Boolean) extends Response

  def apply(): Behavior[Command] = Behaviors.receiveMessage {
    case RegisterUser(nom, email, password, replyTo) =>
      AuthService.registerUser(nom, email, password) match {
        case Success((userId, token)) => replyTo ! AuthResponse(Some(userId), Some(token))
        case Failure(_) => replyTo ! AuthResponse(None, None)
      }
      Behaviors.same

    case AuthenticateUser(email, password, replyTo) =>
      AuthService.authenticate(email, password) match {
        case Some((userId, token)) => replyTo ! AuthResponse(Some(userId), Some(token))
        case None => replyTo ! AuthResponse(None, None)
      }
      Behaviors.same

    case ValidateToken(token, replyTo) =>
      replyTo ! AuthValidationResponse(AuthService.validateToken(token))
      Behaviors.same
  }
}
