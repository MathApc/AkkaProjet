package services

import scala.concurrent.{ExecutionContext, Future}
import repository.StrategyRepository

case class Strategy(id: Int, userId: Int, name: String, symbol: String, parameters: Map[String, Double])

object StrategyService {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  // Recuperer les strategies d'un utilisateur
  def getUserStrategies(userId: Int): Future[List[Strategy]] = {
    StrategyRepository.getUserStrategies(userId)
  }
}
