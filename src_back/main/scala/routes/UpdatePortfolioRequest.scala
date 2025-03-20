package routes

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class UpdatePortfolioRequest(userId: Int, symbol: String, quantity: Double)

object UpdatePortfolioRequest {
  implicit val format: RootJsonFormat[UpdatePortfolioRequest] = jsonFormat3(UpdatePortfolioRequest.apply)
}
