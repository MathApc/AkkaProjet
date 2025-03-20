package actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

import scala.util.{Failure, Success}

object SharpeRatioActor {
  sealed trait Command
  final case class ComputeSharpe(returns: List[Double], riskFreeRate: Double, replyTo: ActorRef[Response]) extends Command

  sealed trait Response
  final case class SharpeResult(value: Double) extends Response

  def apply(): Behavior[Command] = Behaviors.receiveMessage {
    case ComputeSharpe(portfolioReturns, riskFreeRate, replyTo) =>
      val meanReturn = portfolioReturns.sum / portfolioReturns.length
      val stdDev = math.sqrt(portfolioReturns.map(r => math.pow(r - meanReturn, 2)).sum / portfolioReturns.length)
      val sharpeRatio = (meanReturn - riskFreeRate) / stdDev

      replyTo ! SharpeResult(sharpeRatio)
      Behaviors.same
  }
}
