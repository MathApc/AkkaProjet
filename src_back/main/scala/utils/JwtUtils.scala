package utils

import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import java.time.Instant

object JwtUtils {
  val secretKey = "super_secret_key"

  def createToken(userId: Int): String = {
    val claim = JwtClaim(expiration = Some(Instant.now.getEpochSecond + 3600))
    Jwt.encode(claim, secretKey, JwtAlgorithm.HS256)
  }
}
