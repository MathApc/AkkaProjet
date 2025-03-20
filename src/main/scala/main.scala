import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import ch.megard.akka.http.cors.scaladsl.model.{HttpOriginMatcher, HttpHeaderRange}


import actors.{UserActor, PortfolioActor, StrategyActor, NetAssetValueActor}
import routes.{Routes, PortfolioRoutes, StrategyRoutes, FinancialRoutes, MarketDataRoutes}

import scala.concurrent.{Await, ExecutionContextExecutor}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object Main extends App {

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "financial-app")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val userActor = system.systemActorOf(UserActor(), "userActor")
  val portfolioActor = system.systemActorOf(PortfolioActor(), "portfolioActor")
  val strategyActor = system.systemActorOf(StrategyActor(), "strategyActor")
  val navActor = system.systemActorOf(NetAssetValueActor(), "navActor")

  val userRoutes: Route = new Routes(userActor).route
  val portfolioRoutes: Route = new PortfolioRoutes(portfolioActor).routes
  val strategyRoutes: Route = new StrategyRoutes(strategyActor).routes
  val financialRoutes: Route = new FinancialRoutes(navActor).routes
  val marketDataRoutes: Route = new MarketDataRoutes().route

  // Configuration CORS corrigée
  val corsSettings = CorsSettings.defaultSettings
    .withAllowedOrigins(HttpOriginMatcher.*) // Accepte toutes les origines
    .withAllowedMethods(Seq(GET, POST, PUT, DELETE, OPTIONS)) // Méthodes autorisées
    .withAllowedHeaders(HttpHeaderRange.*) // Autorise tous les headers
    .withAllowCredentials(true) // Permet les authentifications
    .withExposedHeaders(Seq("Authorization")) // Expose les headers pour le frontend


  val allRoutes: Route = cors(corsSettings) {
    userRoutes ~ portfolioRoutes ~ strategyRoutes ~ financialRoutes ~ marketDataRoutes
  }

  val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(allRoutes)

  bindingFuture.onComplete {
    case Success(binding) => println(s"Serveur en ligne sur ${binding.localAddress}")
    case Failure(ex) =>
      println(s"Erreur au démarrage du serveur: ${ex.getMessage}")
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
}
