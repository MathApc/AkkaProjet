package streams

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import scala.concurrent.duration._

object MarketDataStream {
  implicit val system: akka.actor.ActorSystem = ActorSystem("MarketDataSystem")


  val source = Source.tick(0.seconds, 1.second, "Fetch Market Data")
  val flow = Flow[String].map(_ => scala.util.Random.nextDouble() * 100)
  val sink = Sink.foreach[Double](price => println(s"Prix mis a jour : $price"))

  def runStream(): Unit = source.via(flow).to(sink).run()
}
