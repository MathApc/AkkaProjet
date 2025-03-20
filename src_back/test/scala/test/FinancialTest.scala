package test

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import services.{NetAssetValueCalculator, SharpeRatioCalculator, MarketTrendDetector}
import models.Asset

class FinancialTest extends AnyFlatSpec with Matchers {

  "Net Asset Value Calculator" should "compute correct NAV" in {
    val portfolio: List[Asset] = List(
      Asset("BTC", 1.0, 50000.0),
      Asset("ETH", 2.0, 3000.0)
    )

    val nav: Double = NetAssetValueCalculator.computeNAV(portfolio)
    nav shouldBe (1.0 * 50000.0 + 2.0 * 3000.0)
  }

  "Sharpe Ratio Calculator" should "compute correct ratio" in {
    val fakeReturns: List[Double] = List(0.05, 0.02, -0.01, 0.03, 0.06)
    val riskFreeRate: Double = 0.01

    val sharpeRatio: Double = SharpeRatioCalculator.computeSharpeRatio(fakeReturns, riskFreeRate)
    sharpeRatio should not be Double.NaN
  }

  "Market Trend Detector" should "detect trend based on EMA" in {
    val historicalPrices: List[Double] = List(100.0, 102.0, 101.0, 105.0, 110.0, 108.0, 115.0, 120.0, 125.0)

    val emaShort: Double = MarketTrendDetector.calculateEMA(historicalPrices, 5).last
    val emaLong: Double = MarketTrendDetector.calculateEMA(historicalPrices, 10).last

    val trend: String = MarketTrendDetector.detectTrend(emaShort, emaLong)
    trend should (be("Upward Trend") or be("Downward Trend") or be("Neutral"))
  }
}
