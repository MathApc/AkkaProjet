package services

import scala.concurrent.{ExecutionContext, Future}
import java.sql.Connection
import database.Database
import routes.UpdatePortfolioRequest

case class PortfolioAsset(symbol: String, quantity: Double)
case class Portfolio(assets: Map[String, PortfolioAsset])



object PortfolioService {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  // Récupérer les actifs d'un portefeuille depuis la base de données
  def getPortfolio(userId: Int): Future[Map[String, Double]] = Future {
    val conn: Connection = Database.getConnection
    val stmt = conn.prepareStatement(
      "SELECT actif, SUM(prix) as total_price FROM price WHERE portefeuille_id = (SELECT id FROM wallet WHERE utilisateur_id = ?) GROUP BY actif"
    )
    stmt.setInt(1, userId)
    val rs = stmt.executeQuery()

    var assets = Map[String, Double]()
    while (rs.next()) {
      assets += (rs.getString("actif") -> rs.getDouble("total_price"))
    }

    conn.close()
    assets
  }

  // Ajouter un actif dans la base de données
  def addAsset(userId: Int, symbol: String, quantity: Double): Future[String] = Future {
    val conn: Connection = Database.getConnection
    val stmt = conn.prepareStatement(
      "INSERT INTO price (portefeuille_id, actif, prix, devise, timestamp) VALUES ((SELECT id FROM wallet WHERE utilisateur_id = ?), ?, ?, 'USD', NOW())"
    )

    stmt.setInt(1, userId)
    stmt.setString(2, symbol)
    stmt.setDouble(3, quantity)

    val rowsInserted = stmt.executeUpdate()
    conn.close()

    if (rowsInserted > 0) s"Ajouté : $quantity $symbol dans le portefeuille."
    else "Erreur lors de l'ajout de l'actif."
  }

  // Supprimer un actif du portefeuille
  def removeAsset(userId: Int, symbol: String): Future[String] = Future {
    val conn: Connection = Database.getConnection
    val stmt = conn.prepareStatement(
      "DELETE FROM price WHERE portefeuille_id = (SELECT id FROM wallet WHERE utilisateur_id = ?) AND actif = ?"
    )

    stmt.setInt(1, userId)
    stmt.setString(2, symbol)

    val rowsDeleted = stmt.executeUpdate()
    conn.close()

    if (rowsDeleted > 0) s"$symbol supprimé du portefeuille."
    else s"$symbol n'est pas présent dans le portefeuille."
  }

  // Mettre à jour un actif
  def updatePortfolio(updateRequest: UpdatePortfolioRequest): Future[Unit] = Future {
    val conn: Connection = Database.getConnection
    val stmt = conn.prepareStatement(
      "UPDATE price SET prix = ? WHERE portefeuille_id = (SELECT id FROM wallet WHERE utilisateur_id = ?) AND actif = ?"
    )

    stmt.setDouble(1, updateRequest.quantity)
    stmt.setInt(2, updateRequest.userId)
    stmt.setString(3, updateRequest.symbol)

    val updatedRows = stmt.executeUpdate()
    conn.close()

    if (updatedRows > 0) {
      println(s"Mise à jour réussie : ${updateRequest.symbol} -> ${updateRequest.quantity}")
    } else {
      println(s"Aucun enregistrement mis à jour pour ${updateRequest.symbol}")
    }
  }
}
