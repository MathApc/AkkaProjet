package services

import scala.util.{Failure, Success, Try}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import java.time.Instant
import java.sql.{Connection, PreparedStatement, ResultSet}
import java.time.LocalDateTime
import database.Database
import spray.json._

case class TokenPayload(userId: Int, email: String)

object AuthService extends DefaultJsonProtocol {

  private val secretKey = "super_secret_key"

  // Format JSON pour sérialiser/désérialiser les claims JWT
  implicit val tokenPayloadFormat: RootJsonFormat[TokenPayload] = jsonFormat2(TokenPayload)

  /** Enregistre un utilisateur et retourne (userId, token) **/
  def registerUser(nom: String, email: String, password: String): Try[(Int, String)] = {
    var conn: Connection = null
    var stmt: PreparedStatement = null
    try {
      conn = Database.getConnection
      stmt = conn.prepareStatement(
        "INSERT INTO users (nom, email, password, date_creation) VALUES (?, ?, ?, ?)",
        java.sql.Statement.RETURN_GENERATED_KEYS
      )

      stmt.setString(1, nom)
      stmt.setString(2, email)
      stmt.setString(3, password)
      stmt.setObject(4, LocalDateTime.now())

      val result = stmt.executeUpdate()
      if (result > 0) {
        val rs: ResultSet = stmt.getGeneratedKeys
        if (rs.next()) {
          val userId = rs.getInt(1)
          val token = generateToken(userId, email)
          rs.close()
          Success((userId, token))
        } else {
          Failure(new Exception("Echec de l'inscription: ID non généré"))
        }
      } else {
        Failure(new Exception("Echec de l'inscription"))
      }
    } catch {
      case e: Exception => Failure(e)
    } finally {
      if (stmt != null) stmt.close()
      if (conn != null) conn.close()
    }
  }

  /** Authentifie un utilisateur et retourne (userId, token) **/
  def authenticate(email: String, password: String): Option[(Int, String)] = {
    var conn: Connection = null
    var stmt: PreparedStatement = null
    var rs: ResultSet = null
    try {
      conn = Database.getConnection
      stmt = conn.prepareStatement("SELECT id, password FROM users WHERE email = ?")
      stmt.setString(1, email)
      rs = stmt.executeQuery()

      if (rs.next() && rs.getString("password") == password) {
        val userId = rs.getInt("id")
        val token = generateToken(userId, email)
        Some((userId, token))
      } else {
        None
      }
    } catch {
      case _: Exception => None
    } finally {
      if (rs != null) rs.close()
      if (stmt != null) stmt.close()
      if (conn != null) conn.close()
    }
  }

  /** Valide un token et retourne `userId` s'il est valide **/
  def validateTokenWithUserId(token: String): Option[Int] = {
    Jwt.decode(token, secretKey, Seq(JwtAlgorithm.HS256)) match {
      case Success(decoded) =>
        decoded.content.parseJson.convertTo[TokenPayload] match {
          case TokenPayload(userId, _) => Some(userId)
          case _ => None
        }
      case Failure(_) => None
    }
  }

  /** Vérifie si un token est valide **/
  def validateToken(token: String): Boolean = {
    Jwt.decode(token, secretKey, Seq(JwtAlgorithm.HS256)).isSuccess
  }

  /** Génère un token JWT contenant userId **/
  private def generateToken(userId: Int, email: String): String = {
    val claim = JwtClaim(
      content = TokenPayload(userId, email).toJson.compactPrint, // Utilisation de spray-json
      issuedAt = Some(Instant.now.getEpochSecond),
      expiration = Some(Instant.now.plusSeconds(3600).getEpochSecond)
    )
    Jwt.encode(claim, secretKey, JwtAlgorithm.HS256)
  }
}
