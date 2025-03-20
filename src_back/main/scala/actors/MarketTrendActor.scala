package actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object MarketTrendActor {
  sealed trait Command
  final case class ComputeTrend(prices: List[Double], replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class TrendResult(trend: String) extends Response

  def apply(): Behavior[Command] = Behaviors.receiveMessage {
    case ComputeTrend(prices, replyTo) =>
      val emaShort = calculateEMA(prices, 3).last
      val emaLong = calculateEMA(prices, 4).last
      val trend = detectTrend(emaShort, emaLong)

      replyTo ! TrendResult(trend)
      Behaviors.same
  }

  private def calculateEMA(prices: List[Double], period: Int): List[Double] = {
    val alpha = 2.0 / (period + 1)
    prices.foldLeft(List.empty[Double]) {
      case (Nil, price) => List(price)
      case (emaValues, price) => emaValues :+ (alpha * price + (1 - alpha) * emaValues.last)
    }
  }

  private def detectTrend(emaShort: Double, emaLong: Double): String = {
    if (emaShort > emaLong) "Uptrend"
    else if (emaShort < emaLong) "Downtrend"
    else "Neutral"
  }
}
