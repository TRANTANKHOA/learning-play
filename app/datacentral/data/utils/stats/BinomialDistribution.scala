package datacentral.data.utils.stats

case class BinomialDistribution(count: Long, mean: Long) {
  require(
    mean > 0 && mean < count,
    s"Please reconfigure the expected # of rows ($mean) to be positive and smaller than available count = $count")
  val p = mean.toDouble / count.toDouble
  val variance = count * p * (1 - p)
  val deviation = math.sqrt(variance)
}
