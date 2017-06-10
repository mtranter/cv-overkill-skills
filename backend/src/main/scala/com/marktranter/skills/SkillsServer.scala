package com.marktranter.skills
import cats.data.OptionT
import cats.instances.future._
import com.marktranter.skills.models.Skill
import fs2.{Strategy, Task}
import org.http4s.{AuthedService, HttpService}
import org.http4s.dsl.Root
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.{CORS, CORSConfig}
import org.http4s.server.blaze._
import org.http4s.util.StreamApp
import com.marktranter.skills.auth._
import fs2.interop.cats._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import org.http4s._
import org.http4s.dsl._
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

  private def getSkill(name: String): Future[Option[Skill]] = for {
    repo <- skillsRepo
    skills <- repo.getSkills()
  } yield skills.find(s => s.name == name)


  case class SkillLevelUpdate(skillLevel: Int)
  case class CreateSkillCommand(name:String, skillLevel: Int)

  val skillsService: AuthedService[User] = AuthedService {
    case GET -> Root as user =>
      Ok(skillsRepo.flatMap(r => r.getSkills().map(s => s.asJson)))
    case req@POST -> Root as AuthorizedUser(AdminRole)  =>
      val json = for {
        skill <- req.req.as(jsonOf[CreateSkillCommand])
        repo <- Task.fromFuture(skillsRepo)
        u <- Task.fromFuture(repo.saveSkill(Skill(skill.name, skill.skillLevel, Set())))
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
    case req@POST -> Root / name / "tags" as AuthorizedUser(AdminRole) =>
        for {
          tag <- req.req.as(jsonOf[String])
          repo <- Task.fromFuture(skillsRepo)
          allOk <- Task.fromFuture(repo.addTag(name,tag))
          status = if(allOk) Status.Ok else Status.NotFound
        } yield Response(status)
  }

  val corsCfg = CORSConfig(
    anyOrigin = true,
    anyMethod = true,
    allowedHeaders = Some(Set("authorization","content-type")),
    allowCredentials = true,
    maxAge = 1.day.toSeconds)
  val service: HttpService = CORS(authMiddleware(roleMiddleware(skillsService)),corsCfg)

  override def stream(args: List[String]): fs2.Stream[Task,Nothing] = {
    BlazeBuilder
      .bindHttp(8080, "0.0.0.0")
      .mountService(service, "/skills")
      .serve
  }
}
