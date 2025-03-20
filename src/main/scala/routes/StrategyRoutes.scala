package routes

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import actors.StrategyActor
import actors.StrategyActor.{ExecuteStrategy, Response}

import scala.concurrent.Future
import scala.concurrent.duration._
import akka.actor.typed.ActorSystem
import scala.concurrent.ExecutionContext

class StrategyRoutes(strategyActor: ActorRef[StrategyActor.Command])(implicit system: ActorSystem[_]) {

  implicit val timeout: Timeout = 10.seconds
  implicit val ec: ExecutionContext = system.executionContext

  val routes: Route =
    pathPrefix("strategy") {
      path("execute") {
        post {
          parameter("userId".as[Int]) { userId =>
            val strategyFuture: Future[Response] =
              strategyActor.ask(ref => ExecuteStrategy(userId, ref))

            complete(strategyFuture.map {
              case StrategyActor.StrategyResult(message) => (StatusCodes.OK, message)
              case _ => (StatusCodes.InternalServerError, "Erreur lors de l'execution de la strategie.")
            })
          }
        }
      }
    }
}
