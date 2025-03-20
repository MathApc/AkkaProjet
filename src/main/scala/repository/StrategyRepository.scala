package repository

import services.Strategy
import scala.concurrent.{ExecutionContext, Future}

object StrategyRepository {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  // Simule une BDD avec des strategies
  private val strategies = List(
    Strategy(1, 1, "Achat BTC", "bitcoin", Map("parameter1" -> 50000.0)),
    Strategy(2, 1, "Vente ETH", "ethereum", Map("parameter1" -> 3000.0))
  )

  def getUserStrategies(userId: Int): Future[List[Strategy]] = Future {
    strategies.filter(_.userId == userId)
  }
}
