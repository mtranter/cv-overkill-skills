package com.marktranter.skills.auth

import java.net.URL

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.RemoteJWKSet
import com.nimbusds.jose.proc.{JWSVerificationKeySelector, SecurityContext}
import com.nimbusds.jose.util.DefaultResourceRetriever
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import scala.collection.JavaConverters._
import scala.util.Try

trait TokenValidator {
  def validate(token: String): Try[Map[String,Object]]
}

object JwtTokenValidator {
  def apply(keysUrl: String) = new JwtTokenValidator(keysUrl)
}

class JwtTokenValidator(keysUrl: String) extends TokenValidator{
  private object DummyContext extends SecurityContext
  private val jwtProcessor = new DefaultJWTProcessor[DummyContext.type]()
  private val keySource = new RemoteJWKSet[DummyContext.type](new URL(keysUrl), new DefaultResourceRetriever(2000,2000))
  private val expectedJWSAlg = JWSAlgorithm.RS256
  private val keySelector = new JWSVerificationKeySelector[DummyContext.type](expectedJWSAlg, keySource)
  jwtProcessor.setJWSKeySelector(keySelector)

  override def validate(token: String): Try[Map[String, AnyRef]] = Try {
    try {
      println(token)
      jwtProcessor.process(token, DummyContext).getClaims.asScala.toMap
    } catch {
      case e:Throwable => println(e.getLocalizedMessage); throw e
    }
  }
}