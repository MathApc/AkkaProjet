package routes

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import actors.PortfolioActor
import actors.PortfolioActor.{AddAsset, GetPortfolio, RemoveAsset, Response}
import akka.http.scaladsl.server.Route
import spray.json._
import models.{Portfolio, PortfolioJsonProtocol}
import services.PortfolioService

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

// Ajout des imports pour JSON
import routes.UpdatePortfolioRequest
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class PortfolioRoutes(portfolioActor: ActorRef[PortfolioActor.Command])(implicit system: ActorSystem[_]) {

  implicit val timeout: Timeout = 10.seconds
  implicit val ec: ExecutionContext = system.executionContext
  import PortfolioJsonProtocol._

  val routes: Route =
    pathPrefix("portfolio") {
      concat(
        pathEnd {
          get {
            parameter("userId".as[Int]) { userId =>
              val portfolioFuture: Future[Response] = portfolioActor.ask(ref => GetPortfolio(userId, ref))(timeout, system.scheduler)
              
              onSuccess(portfolioFuture) {
                case PortfolioActor.PortfolioData(assets) => complete(Portfolio(assets).toJson.toString())
                case _ => complete(StatusCodes.InternalServerError, "Erreur lors de la récupération du portefeuille.")
              }
            }
          }
        },
        path("add") {
          post {
            parameters("userId".as[Int], "symbol", "quantity".as[Double]) { (userId, symbol, quantity) =>
              val addFuture: Future[Response] = portfolioActor.ask(ref => AddAsset(userId, symbol, quantity, ref))(timeout, system.scheduler)
              
              onSuccess(addFuture) {
                case PortfolioActor.OperationResult(message) => complete(StatusCodes.OK, message)
                case _ => complete(StatusCodes.InternalServerError, "Erreur lors de l'ajout de l'actif.")
              }
            }
          }
        },
        path("remove") {
          delete {
            parameters("userId".as[Int], "symbol") { (userId, symbol) =>
              val removeFuture: Future[Response] = portfolioActor.ask(ref => RemoveAsset(userId, symbol, ref))(timeout, system.scheduler)
              
              onSuccess(removeFuture) {
                case PortfolioActor.OperationResult(message) => complete(StatusCodes.OK, message)
                case _ => complete(StatusCodes.InternalServerError, "Erreur lors de la suppression de l'actif.")
              }
            }
          }
        },
        path("update") {
          put {
            entity(as[UpdatePortfolioRequest]) { updateRequest =>
              val updateResult = PortfolioService.updatePortfolio(updateRequest)
              onComplete(updateResult) {
                case Success(_) => complete(StatusCodes.OK, "Mise à jour réussie")
                case Failure(ex) => complete(StatusCodes.InternalServerError, s"Erreur: ${ex.getMessage}")
              }
            }
          }
        }
      )
    }
}
