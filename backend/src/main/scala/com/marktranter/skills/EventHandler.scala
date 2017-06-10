package com.marktranter.skills

import com.marktranter.skills.models.Skill
import reactivemongo.api.DefaultDB

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mark on 29/05/17.
  */

abstract class DomainEvent
case class ExperienceSkillAdded(companyName: String, skill: String) extends DomainEvent
case class ExperienceSkillRemoved(companyName: String, skill: String) extends DomainEvent

trait EventHandler {
  def handle[T <: DomainEvent]: PartialFunction[T, Future[Unit]]
}

class EventDispatcher(handlers: Seq[EventHandler]) {
  def dispatch[T <: DomainEvent](t: T) = handlers find { _.handle.isDefinedAt(t) } map { _.handle(t) }
}

class SkillsEventHandler(repo: SkillsRepository)(implicit ec: ExecutionContext) extends EventHandler {
  override def handle[T <: DomainEvent]: PartialFunction[T, Future[Unit]] = {
    case ExperienceSkillAdded(co, skill) => {
      repo.getSkills() map { s =>
        s.find(s => s.name == skill) match {
          case None => repo.saveSkill(Skill(skill, 50,Set()))
          case _ => Future.unit
        }
      }
    }
    case _ => Future.unit
  }
}