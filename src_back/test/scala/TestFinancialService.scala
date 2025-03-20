import services.{NetAssetValueCalculator, SharpeRatioCalculator, MarketTrendDetector}

object TestFinancialService extends App {
  println("NAV User 1: " + NetAssetValueCalculator.computeNAV(1))
  println("Sharpe Ratio User 1: " + SharpeRatioCalculator.computeSharpeRatio(1, 0.01))
  println("Tendance BTC: " + MarketTrendDetector.detectTrend("bitcoin"))
}
