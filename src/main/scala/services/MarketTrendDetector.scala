package services

import repository.FinancialRepository

object MarketTrendDetector {

  def calculateEMA(prices: List[Double], period: Int): List[Double] = {
    val alpha = 2.0 / (period + 1)
    prices.foldLeft(List.empty[Double]) { (emaValues, price) =>
      emaValues match {
        case Nil => List(price)
        case _   => emaValues :+ (alpha * price + (1 - alpha) * emaValues.last)
      }
    }
  }

  def detectTrend(symbol: String): String = {
    val prices = (1 to 10).flatMap(_ => FinancialRepository.getPrice(symbol)).toList
    if (prices.length < 10) return "Pas assez de donnees"

    val emaShort = calculateEMA(prices, 5).last
    val emaLong = calculateEMA(prices, 10).last

    if (emaShort > emaLong) "Tendance Haussiere"
    else if (emaShort < emaLong) "Tendance Baissiere"
    else "Neutre"
  }
}
