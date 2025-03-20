package repository

import database.Database

import models.{Wallet, Price, User}
import java.sql.Connection

object FinancialRepository {

  def getPrice(actif: String): Option[Double] = {
    val conn: Connection = Database.getConnection
    try {
      val stmt = conn.prepareStatement("SELECT prix FROM price WHERE actif = ?")
      stmt.setString(1, actif)
      val rs = stmt.executeQuery()
      if (rs.next()) Some(rs.getDouble("prix")) else None
    } finally {
      conn.close()
    }
  }

  def getUserPortfolio(userId: Int): List[Wallet] = {
    val conn: Connection = Database.getConnection
    try {
      val stmt = conn.prepareStatement("SELECT * FROM wallet WHERE utilisateur_id = ?")
      stmt.setInt(1, userId)
      val rs = stmt.executeQuery()
      var wallets = List.empty[Wallet]

      while (rs.next()) {
        wallets ::= Wallet(
          rs.getInt("id"),
          rs.getInt("utilisateur_id"),
          rs.getString("nom"),
          rs.getString("date_creation")
        )
      }
      wallets
    } finally {
      conn.close()
    }
  }
}
