package actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import services.{FinancialService, MarketDataService}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object NetAssetValueActor {
  sealed trait Command
  final case class ComputeNAV(userId: Int, useRealTimeData: Boolean, replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class MetricsResult(NAV: Double, sharpeRatio: Double, marketTrend: Double) extends Response

  def apply()(implicit ec: ExecutionContext): Behavior[Command] = Behaviors.setup { context =>
    Behaviors.receiveMessage {
      case ComputeNAV(userId, useRealTimeData, replyTo) =>
        println(s"[NetAssetValueActor] Début du calcul NAV pour userId=$userId, realTime=$useRealTimeData")

        val futureNAV: Future[Double] = FinancialService.getPortfolio(userId).flatMap { portfolio =>
          println(s"[NetAssetValueActor] Portefeuille récupéré: $portfolio")

          if (useRealTimeData) {
            val symbols = portfolio.map(_.symbol)
            println(s"[NetAssetValueActor] Récupération des prix en temps réel pour: $symbols")

            MarketDataService.fetchMultipleCryptoPrices(symbols).map { marketPrices =>
              portfolio.map { asset =>
                val realTimePrice = marketPrices.find(_.symbol == asset.symbol).map(_.price).getOrElse(asset.price)
                asset.quantity * realTimePrice
              }.sum
            }
          } else {
            println(s"[NetAssetValueActor] Utilisation des prix stockés en base")
            Future.successful(portfolio.map(asset => asset.quantity * asset.price).sum)
          }
        }

        val futureMetrics: Future[MetricsResult] = futureNAV.flatMap { nav =>
          val btcPriceHistoryFuture = MarketDataService.getPriceHistory("bitcoin")

          val marketTrendFuture: Future[Double] = btcPriceHistoryFuture.map(prices => calculerEMA(prices, 10))
          val sharpeRatioFuture: Future[Double] = btcPriceHistoryFuture.map(prices => calculerSharpeRatio(prices, 0.02))


          for {
            marketTrend <- marketTrendFuture
            sharpeRatio <- sharpeRatioFuture
          } yield MetricsResult(nav, sharpeRatio, marketTrend)
        }

        futureMetrics.onComplete {
          case Success(metrics) =>
            println(s"[NetAssetValueActor] Résultat calculé: NAV=${metrics.NAV}, Sharpe=${metrics.sharpeRatio}, Market Trend=${metrics.marketTrend}")
            replyTo ! metrics
          case Failure(ex) =>
            println(s"[NetAssetValueActor] Erreur de calcul: ${ex.getMessage}")
            replyTo ! MetricsResult(0.0, 0.0, 0.0)
        }

        Behaviors.same
    }
  }

  // Fonction pour calculer l'Exponential Moving Average (EMA)
  private def calculerEMA(prices: List[Double], period: Int = 10): Double = {
    if (prices.length < period) return prices.lastOption.getOrElse(0.0)
    val multiplier = 2.0 / (period + 1)
    prices.foldLeft(prices.head) { (ema, price) =>
      (price - ema) * multiplier + ema
    }
  }

  // Fonction pour calculer le Sharpe Ratio
  private def calculerSharpeRatio(prices: List[Double], riskFreeRate: Double = 0.02): Double = {
    if (prices.length < 2) return 0.0
    val returns = prices.sliding(2).collect { case List(prev, curr) => (curr - prev) / prev }.toList
    val avgReturn = returns.sum / returns.length
    val stdDev = math.sqrt(returns.map(r => math.pow(r - avgReturn, 2)).sum / (returns.length - 1))
    if (stdDev == 0) 0.0 else (avgReturn - riskFreeRate) / stdDev
  }
}
