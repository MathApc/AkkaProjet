package services

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer
import spray.json._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import spray.json.DefaultJsonProtocol._

object MarketDataService {

  private val apiUrl = "https://api.coingecko.com/api/v3/simple/price?ids="
  
  implicit val system: ActorSystem = ActorSystem("MarketDataSystem")
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext = system.dispatcher

  case class MarketData(symbol: String, price: Double)

  object MarketDataJsonProtocol extends DefaultJsonProtocol {
    implicit val marketDataFormat: RootJsonFormat[MarketData] = jsonFormat2(MarketData)
    implicit val marketDataListFormat: RootJsonFormat[List[MarketData]] = new RootJsonFormat[List[MarketData]] {
      def write(obj: List[MarketData]): JsValue = JsArray(obj.map(_.toJson).toVector)
      def read(json: JsValue): List[MarketData] = json match {
        case JsArray(elements) => elements.map(_.convertTo[MarketData]).toList
        case _                 => throw new DeserializationException("Expected a JSON array")
      }
    }
  }

  import MarketDataJsonProtocol._

  val trackedAssets: List[String] = List("bitcoin", "ethereum", "aapl", "tsla", "eurusd")

  private var lastFetchedPrices: Map[String, MarketData] = Map()
  private var lastFetchTime: Long = 0
  private val cacheDuration: Long = 60 * 1000 // 1 minute

  def fetchMultipleCryptoPrices(symbols: List[String]): Future[List[MarketData]] = {
    val currentTime = System.currentTimeMillis()

    if (currentTime - lastFetchTime < cacheDuration && lastFetchedPrices.nonEmpty) {
      println("[MarketDataService] Utilisation du cache pour éviter une surcharge API.")
      return Future.successful(lastFetchedPrices.values.toList)
    }

    if (symbols.isEmpty) {
      println("[MarketDataService] Aucune requête effectuée : liste de symboles vide.")
      return Future.successful(List.empty)
    }

    val formattedSymbols = symbols.map(_.toLowerCase).mkString(",")
    val requestUrl = s"$apiUrl$formattedSymbols&vs_currencies=usd"

    println(s"[MarketDataService] Requête envoyée à CoinGecko: $requestUrl")

    val request = HttpRequest(uri = requestUrl)

    Http().singleRequest(request).flatMap { response =>
      response.entity.toStrict(5.seconds).map(_.data.utf8String).map { jsonString =>
        println(s"[MarketDataService] Réponse brute reçue : $jsonString")

        try {
          val json = jsonString.parseJson.asJsObject

          val marketDataList = symbols.flatMap { symbol =>
            json.fields.get(symbol.toLowerCase) match {
              case Some(data) =>
                data.asJsObject.fields.get("usd") match {
                  case Some(price) =>
                    val marketData = MarketData(symbol, price.convertTo[Double])
                    lastFetchedPrices += (symbol -> marketData)
                    Some(marketData)
                  case None =>
                    println(s"[MarketDataService] Clé 'usd' non trouvée pour $symbol dans la réponse API.")
                    None
                }
              case None =>
                println(s"[MarketDataService] Actif $symbol non trouvé dans la réponse API.")
                None
            }
          }

          lastFetchTime = System.currentTimeMillis()
          println(s"[MarketDataService] Données traitées : $marketDataList")
          marketDataList

        } catch {
          case ex: Exception =>
            println(s"[MarketDataService] Erreur lors du parsing JSON : ${ex.getMessage}")
            lastFetchedPrices.values.toList
        }
      }
    }.recover {
      case ex =>
        println(s"[MarketDataService] Erreur lors de la récupération des données : ${ex.getMessage}")
        lastFetchedPrices.values.toList
    }
  }

  def getPriceHistory(symbol: String, days: Int = 30): Future[List[Double]] = Future {
    println(s"[MarketDataService] Récupération de l'historique des prix pour $symbol sur $days jours.")

    val conn = database.Database.getConnection
    val stmt = conn.prepareStatement("SELECT prix FROM price WHERE actif = ? ORDER BY timestamp DESC LIMIT ?")
    stmt.setString(1, symbol)
    stmt.setInt(2, days)
    val rs = stmt.executeQuery()

    val prices = Iterator.continually((rs, rs.next())).takeWhile(_._2).map(_._1.getDouble("prix")).toList
    conn.close()

    println(s"[MarketDataService] Historique récupéré pour $symbol : $prices")
    prices
  }
}
