package com.marktranter.skills
import com.marktranter.skills.models.Skill
import fs2.{Strategy, Task}
import org.http4s.{AuthedService, HttpService}
import org.http4s.dsl.Root
import org.http4s.server.{AuthMiddleware}
import org.http4s.server.middleware.CORS
import org.http4s.server.blaze._
import org.http4s.util.StreamApp
import com.marktranter.skills.auth._

import scala.concurrent.{ExecutionContext, Future}
import org.http4s._, org.http4s.dsl._
import org.http4s.circe._
import io.circe.syntax._
import io.circe.generic.auto._
/**
  * Created by mark on 29/05/17.
  */



object Main extends StreamApp {

  implicit val ec = ExecutionContext.global
  implicit val strategy = Strategy.fromExecutionContext(ec)
  val connectionUrl = sys.env.getOrElse("MONGO_CONNECTION","mongodb://localhost/")

  println("Using mongo connection " + connectionUrl)
  val skillsRepo = MongoSkillsRepository.init(connectionUrl +"skills")

  skillsRepo.map(r => {
    MongoEventBusListener.init(
      connectionUrl + "eventhub",
      new EventDispatcher( Seq(new SkillsEventHandler(r)))).map(s => new s.EventStreamListener("experience.skill.changed"))
  })

  val authMiddleware = AuthMiddleware(Authentication(JwtTokenValidator("https://marktranter.eu.auth0.com/.well-known/jwks.json")))
  var roleMiddleware = ClaimsMiddleware(ClaimsAuthorization(Map("sub" -> "github|3257273")))

  case class SkillLevelUpdate(skillLevel: Int)

  val skillsService: AuthedService[User] = AuthedService {
    case GET -> Root as user =>
      Ok(skillsRepo.flatMap(r => r.getSkills().map(s => s.asJson)))
    case req@POST -> Root as AuthorizedUser(AdminRole)  =>
      val json = for {
        skill <- req.req.as(jsonOf[Skill])
        repo <- Task.fromFuture(skillsRepo)
        u <- Task.fromFuture(repo.saveSkill(skill))
      } yield skill.asJson
      Created(json)
    case req@PATCH -> Root / name as AuthorizedUser(AdminRole) =>
      for {
        s <- for {
          newLevel <- req.req.as(jsonOf[SkillLevelUpdate])
          repo <- Task.fromFuture(skillsRepo)
          skills <- Task.fromFuture(repo.getSkills())
        } yield skills.find(s => s.name == name) match {
          case Some(sk) => repo.updateSkill(name, newLevel.skillLevel).map(_ => Status.Ok)
          case None => Future.successful(Status.NotFound)
        }
        f <- Task.fromFuture(s)
      } yield Response(f)
    case req@DELETE -> Root / name as AuthorizedUser(AdminRole) => Ok(skillsRepo.flatMap(r => r.deleteSkill(name)))
  }

  val service: HttpService = CORS(authMiddleware(roleMiddleware(skillsService)))

  override def stream(args: List[String]): fs2.Stream[Task,Nothing] = {
    BlazeBuilder
      .bindHttp(8080, "0.0.0.0")
      .mountService(service, "/skills")
      .serve
  }
}
