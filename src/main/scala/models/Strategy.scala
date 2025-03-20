package models

case class Strategy(id: Int, userId: Int, name: String, parameters: Map[String, Double])
