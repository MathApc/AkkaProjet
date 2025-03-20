package routes

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import actors.NetAssetValueActor
import akka.actor.typed.ActorRef
import spray.json._
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

// Définition du protocole JSON pour la sérialisation
object FinancialJsonProtocol extends DefaultJsonProtocol {
  implicit val financialMetricsFormat: RootJsonFormat[NetAssetValueActor.MetricsResult] = jsonFormat3(NetAssetValueActor.MetricsResult)
}

class FinancialRoutes(navActor: ActorRef[NetAssetValueActor.Command])(implicit system: ActorSystem[_]) {

  import FinancialJsonProtocol._

  implicit val timeout: Timeout = 5.seconds
  implicit val ec: scala.concurrent.ExecutionContextExecutor = system.executionContext

  val routes = pathPrefix("financial") {
    path("metrics") {
      get {
        parameter("userId".as[Int]) { userId =>
          println(s"[FinancialRoutes] Requête reçue pour userId: $userId")

          val metricsFuture: Future[NetAssetValueActor.Response] = 
            navActor.ask(ref => NetAssetValueActor.ComputeNAV(userId, useRealTimeData = false, ref))

          onComplete(metricsFuture) {
            case Success(metrics: NetAssetValueActor.MetricsResult) =>
              println(s"[FinancialRoutes] Réponse envoyée: NAV=${metrics.NAV}, Sharpe=${metrics.sharpeRatio}, EMA=${metrics.marketTrend}")
              complete(StatusCodes.OK, metrics.toJson)

            case Failure(ex) => 
              println(s"[FinancialRoutes] Erreur: ${ex.getMessage}")
              complete(StatusCodes.InternalServerError, s"Erreur: ${ex.getMessage}")
          }
        }
      }
    }
  }
}
