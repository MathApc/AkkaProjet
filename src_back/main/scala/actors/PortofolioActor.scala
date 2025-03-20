package actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import services.PortfolioService
import scala.util.{Failure, Success}

// Définition des commandes
object PortfolioActor {
  sealed trait Command
  final case class GetPortfolio(userId: Int, replyTo: ActorRef[Response]) extends Command
  final case class AddAsset(userId: Int, symbol: String, quantity: Double, replyTo: ActorRef[Response]) extends Command
  final case class RemoveAsset(userId: Int, symbol: String, replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class PortfolioData(assets: Map[String, Double]) extends Response
  final case class OperationResult(message: String) extends Response

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    implicit val ec = context.executionContext

    Behaviors.receiveMessage {
      case GetPortfolio(userId, replyTo) =>
        PortfolioService.getPortfolio(userId).onComplete {
          case Success(portfolio) => replyTo ! PortfolioData(portfolio)
          case Failure(ex)        => replyTo ! OperationResult(s"Erreur : ${ex.getMessage}")
        }
        Behaviors.same

      case AddAsset(userId, symbol, quantity, replyTo) =>
        PortfolioService.addAsset(userId, symbol, quantity).onComplete {
          case Success(message) => replyTo ! OperationResult(message)
          case Failure(ex)      => replyTo ! OperationResult(s"Erreur : ${ex.getMessage}")
        }
        Behaviors.same

      case RemoveAsset(userId, symbol, replyTo) =>
        PortfolioService.removeAsset(userId, symbol).onComplete {
          case Success(message) => replyTo ! OperationResult(message)
          case Failure(ex)      => replyTo ! OperationResult(s"Erreur : ${ex.getMessage}")
        }
        Behaviors.same
    }
  }
}
