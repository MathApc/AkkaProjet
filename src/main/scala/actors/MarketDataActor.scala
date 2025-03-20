package actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import services.MarketDataService
import services.MarketDataService.MarketData
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object MarketDataActor {

  sealed trait Command
  case class FetchMarketData(symbol: String, replyTo: ActorRef[Response]) extends Command
  private case class WrappedResponse(response: Response) extends Command

  sealed trait Response
  case class MarketPrice(symbol: String, price: Double) extends Response
  case object MarketDataUnavailable extends Response

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    implicit val ec: ExecutionContext = context.system.executionContext 

    Behaviors.receiveMessage {
      case FetchMarketData(symbol, replyTo) =>
        context.pipeToSelf(MarketDataService.fetchMultipleCryptoPrices(List(symbol)).map(_.headOption)) {
          case Success(Some(MarketData(symbol, price))) => WrappedResponse(MarketPrice(symbol, price))
          case _ => WrappedResponse(MarketDataUnavailable)
        }
        Behaviors.same

      case WrappedResponse(response) =>
        response match {
          case MarketPrice(symbol, price) =>
            context.log.info(s"Prix mis a jour pour $symbol: $$${price}")
          case MarketDataUnavailable =>
            context.log.warn("Les donnees de marche ne sont pas disponibles.")
        }
        Behaviors.same
    }
  }
}
