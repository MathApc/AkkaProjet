package services

import repository.FinancialRepository

object SharpeRatioCalculator {

  def computeSharpeRatio(userId: Int, riskFreeRate: Double): Option[Double] = {
    val wallets = FinancialRepository.getUserPortfolio(userId)
    if (wallets.isEmpty) return None

    val prices = wallets.flatMap(wallet => FinancialRepository.getPrice(wallet.walletName))
    if (prices.isEmpty) return None

    val meanReturn = prices.sum / prices.length
    val stdDev = math.sqrt(prices.map(r => math.pow(r - meanReturn, 2)).sum / prices.length)

    if (stdDev == 0) None else Some((meanReturn - riskFreeRate) / stdDev)
  }
}
