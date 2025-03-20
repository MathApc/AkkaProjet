package services

import database.Database
import scala.concurrent.{ExecutionContext, Future}
import java.sql.{Connection, PreparedStatement, ResultSet}
import models.Asset
import java.sql.Connection
import scala.concurrent.{ExecutionContext, Future}
import models.Asset
import database.Database

object FinancialService {
  
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  def getPortfolio(userId: Int)(implicit ec: ExecutionContext): Future[List[Asset]] = Future {
    val conn: Connection = Database.getConnection
    val statement = conn.prepareStatement("""
      SELECT p.actif, SUM(p.prix) AS prix, COUNT(p.actif) AS quantity
      FROM price p
      JOIN wallet w ON p.portefeuille_id = w.id
      WHERE w.utilisateur_id = ?
      GROUP BY p.actif
    """)

    statement.setInt(1, userId)
    val resultSet = statement.executeQuery()

    var portfolio: List[Asset] = List()
    while (resultSet.next()) {
      portfolio = portfolio :+ Asset(
        resultSet.getString("actif"),
        resultSet.getDouble("quantity"),
        resultSet.getDouble("prix")
      )
    }
    conn.close()
    
    println(s"[FinancialService] Portefeuille récupéré pour userId=$userId : $portfolio")
    portfolio
  }

  def computeNAV(userId: Int): Future[Double] = {
    getPortfolio(userId).map { portfolio =>
      portfolio.map(asset => asset.quantity * asset.price).sum
    }
  }

  def computeSharpeRatio(userId: Int): Future[Double] = {
    getPortfolio(userId).map { portfolio =>
      val averageReturn = portfolio.map(asset => asset.price * 0.02).sum / portfolio.size // Exemple de rendement moyen
      val riskFreeRate = 0.01 // Taux sans risque
      val standardDeviation = 0.05 // Exemple de volatilité
      (averageReturn - riskFreeRate) / standardDeviation
    }
  }

  def computeEMA(userId: Int): Future[Double] = {
    getPortfolio(userId).map { portfolio =>
      portfolio.map(_.price).sum / portfolio.size // Moyenne exponentielle simulée
    }
  }

  def getFinancialMetrics(userId: Int): Future[Map[String, Double]] = {
    for {
      nav <- computeNAV(userId)
      sharpe <- computeSharpeRatio(userId)
      ema <- computeEMA(userId)
    } yield Map("nav" -> nav, "sharpeRatio" -> sharpe, "marketTrend" -> ema)
  }
}
