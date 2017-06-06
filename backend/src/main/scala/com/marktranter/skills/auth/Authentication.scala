package com.marktranter.skills.auth

import cats.data.Kleisli
import fs2.{Strategy, Task}
import org.http4s.headers.{Authorization => AuthHeader}
import org.http4s.{Request, Service}
/**
  * Created by mark on 03/06/17.
  */
sealed abstract class Role
case object UserRole extends Role
case object AdminRole extends Role

sealed abstract class User {
  val isAuthenticated: Boolean
}
case class AuthorizedUser(role: Role) extends User { val isAuthenticated = true}
case class AuthenticatedUser(claims: Map[String, AnyRef]) extends User { val isAuthenticated = true}
case object AnonymousUser extends User { val isAuthenticated = false}


object Authentication {

  private val bearerPattern = "(Bearer\\s)(.+)".r

  def apply(tokenValidator: TokenValidator)(implicit s: Strategy): Service[Request, User] = Kleisli{ r =>

    val claimsE = for {
      h <- r.headers.get(AuthHeader).toRight("No Authorization Header Present")
      tokenWithBearer <- bearerPattern.findFirstMatchIn(h.value).toRight("No bearer token present")
      token = tokenWithBearer.group(2)
      c <- tokenValidator.validate(token).toEither
    } yield c

    claimsE match {
      case Left(_) => Task(AnonymousUser)
      case Right(c) => Task(AuthenticatedUser(c))
    }
  }
}

