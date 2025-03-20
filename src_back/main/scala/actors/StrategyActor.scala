package actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import services.{MarketDataService, StrategyService}
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object StrategyActor {

  sealed trait Command
  final case class ExecuteStrategy(userId: Int, replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class StrategyResult(message: String) extends Response

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    implicit val ec: ExecutionContext = context.executionContext

    Behaviors.receiveMessage {
      case ExecuteStrategy(userId, replyTo) =>
        context.log.info(s"[StrategyActor] Execution des strategies pour l'utilisateur $userId")

        val strategiesFuture = StrategyService.getUserStrategies(userId)

        strategiesFuture.onComplete {
          case Success(strategies) =>
            if (strategies.isEmpty) {
              replyTo ! StrategyResult("Aucune strategie trouvee.")
            } else {
              val symbols = strategies.map(_.symbol).distinct

              // Recupere les prix pour tous les symboles en une seule requete
              val pricesFuture = MarketDataService.fetchMultipleCryptoPrices(symbols)

              pricesFuture.onComplete {
                case Success(marketDataList) =>
                  val marketDataMap = marketDataList.map(data => data.symbol -> data.price).toMap

                  val results = strategies.flatMap { strategy =>
                    marketDataMap.get(strategy.symbol).map { price =>
                      val decision =
                        if (price > strategy.parameters.getOrElse("parameter1", Double.MaxValue)) "Buy"
                        else "Sell"

                      s"Strategie '${strategy.name}': $decision a $price USD"
                    }
                  }

                  replyTo ! StrategyResult(results.mkString("\n"))

                case Failure(ex) =>
                  context.log.error(s"Erreur lors de la recuperation des donnees de marche: ${ex.getMessage}")
                  replyTo ! StrategyResult("Erreur lors de l'execution de la strategie.")
              }
            }

          case Failure(ex) =>
            context.log.error(s"Erreur lors de la recuperation des strategies: ${ex.getMessage}")
            replyTo ! StrategyResult("Erreur lors de la recuperation des strategies.")
        }

        Behaviors.same
    }
  }
}
