package models

import spray.json._

final case class Portfolio(assets: Map[String, Double])

object PortfolioJsonProtocol extends DefaultJsonProtocol {
  implicit val portfolioFormat: RootJsonFormat[Portfolio] = jsonFormat1(Portfolio)
}
