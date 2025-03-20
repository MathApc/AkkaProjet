package routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Source}
import akka.http.scaladsl.server.Route
import akka.actor.typed.ActorSystem
import spray.json._
import DefaultJsonProtocol._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import services.MarketDataService
import scala.concurrent.Future
import scala.util.{Failure, Success}
import services.MarketDataService.MarketDataJsonProtocol._

class MarketDataRoutes()(implicit system: ActorSystem[_]) {

  implicit val ec: scala.concurrent.ExecutionContextExecutor = system.executionContext

  // Log pour vérifier si la classe est bien chargée
  println("[MarketDataRoutes] Initialisation...")

  private val singlePriceRoute: Route =
    path("market" / "price") {
      get {
        parameter("symbol") { symbol =>
          val priceFuture: Future[Option[MarketDataService.MarketData]] =
            MarketDataService.fetchMultipleCryptoPrices(List(symbol)).map(_.headOption)

          onComplete(priceFuture) {
            case Success(Some(data)) => complete(HttpEntity(ContentTypes.`application/json`, data.toJson.prettyPrint))
            case Success(None) => complete(StatusCodes.NotFound, "Symbol not found")
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
          }
        }
      }
    }

  private val multiplePricesRoute: Route =
    path("market" / "prices") {
      get {
        parameter("symbols".?) { symbolsOpt =>
          val symbols = symbolsOpt.map(_.split(",").toList.map(_.trim.toUpperCase)).getOrElse(MarketDataService.trackedAssets)

          val pricesFuture: Future[List[MarketDataService.MarketData]] =
            MarketDataService.fetchMultipleCryptoPrices(symbols)

          onComplete(pricesFuture) {
            case Success(data) if data.nonEmpty => complete(HttpEntity(ContentTypes.`application/json`, data.toJson.prettyPrint))
            case Success(_) => complete(StatusCodes.NotFound, "No symbols found")
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"Error: ${ex.getMessage}")
          }
        }
      }
    }

  private val marketUpdatesRoute: Route =
    path("market" / "updates") {
      handleWebSocketMessages(newMarketUpdatesFlow)
    }

  private def newMarketUpdatesFlow: Flow[Message, Message, Any] = {
    val updates = Source
      .tick(scala.concurrent.duration.Duration.Zero, scala.concurrent.duration.FiniteDuration(1, "second"), "Market Update")
      .mapAsync(1)(_ => MarketDataService.fetchMultipleCryptoPrices(MarketDataService.trackedAssets))
      .map(prices => TextMessage(prices.toJson.prettyPrint))

    Flow.fromSinkAndSourceCoupled(akka.stream.scaladsl.Sink.ignore, updates)
  }

  val route: Route =
    cors() {
      pathPrefix("market") {
        concat(
          singlePriceRoute,
          multiplePricesRoute,
          marketUpdatesRoute
        )
      }
    }
}
