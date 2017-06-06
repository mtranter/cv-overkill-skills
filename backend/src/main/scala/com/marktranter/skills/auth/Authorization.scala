package com.marktranter.skills.auth

import cats.data.Kleisli
import fs2.interop.cats._
import fs2._
import org.http4s.server.{AuthMiddleware, Middleware}
import org.http4s._
import org.http4s.headers.Authorization

/**
  * Created by mark on 05/06/17.
  */
object ClaimsMiddleware  {
  type PassThroughMiddleware[Req,Res] = Middleware[Req, Res, Req, Res]
  type ClaimsAuthorizationMiddleware = PassThroughMiddleware[AuthedRequest[User], MaybeResponse]

  def apply(authedUser:  Service[AuthedRequest[User], User])(implicit s: Strategy): ClaimsAuthorizationMiddleware = {
    val inner: Kleisli[Task,AuthedRequest[User], AuthedRequest[User]] = Kleisli({ r:AuthedRequest[User] =>
      authedUser.run(r).map(u => AuthedRequest(u,r.req))
    })
    service => service.compose(inner)
  }
}

object ClaimsAuthorization {

  private val bearerPattern = "(Bearer\\s)(.+)".r

  def apply(adminClaims: Map[String, AnyRef])(implicit s: Strategy): Service[AuthedRequest[User], User] = Kleisli{ r =>
    val retval: User = r.authInfo match {
      case u:AuthorizedUser => u
      case AuthenticatedUser(c) => if(adminClaims.exists((kv) => c.exists((kv1) => kv._1 == kv1._1 && kv._2 == kv1._2))) {
        AuthorizedUser(AdminRole)
      } else {
        AuthorizedUser(UserRole)
      }
      case u@AnonymousUser => u
    }
    Task(retval)
  }
}