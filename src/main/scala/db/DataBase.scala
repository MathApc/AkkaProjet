package database

import java.sql.{Connection, DriverManager}

object Database {
  private val url = "jdbc:mysql://localhost:3306/dbakka"
  private val user = "root"
  private val password = "cytech0001"

  def getConnection: Connection = {
    DriverManager.getConnection(url, user, password)
  }
}
