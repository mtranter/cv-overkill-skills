package com.marktranter.skills

import com.marktranter.skills.models.Skill
import reactivemongo.api.Cursor.{Cont, Done}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.api.collections.bson.BSONCollectionProducer
import reactivemongo.bson.{BSONDocument, Macros}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by mark on 29/05/17.
  */
trait SkillsRepository {
  def saveSkill(skill: Skill): Future[Unit]
  def getSkills(): Future[Seq[Skill]]
  def deleteSkill(name: String): Future[Unit]
  def updateSkill(name: String, skillLevel: Int): Future[Unit]
  def addTag(name: String, tag: String): Future[Boolean]
  def deleteTag(name: String, tag: String): Future[Boolean]
}


object MongoSkillsRepository {

  val driver = new MongoDriver

  def init(mongoUri: String)(implicit ec: ExecutionContext): Future[MongoSkillsRepository] = for {
    uri <- Future.fromTry(MongoConnection.parseURI(mongoUri))
    con = driver.connection(uri)
    dn <- Future(uri.db.get)
    db <- con.database(dn)
  } yield new MongoSkillsRepository(db)
}


class MongoSkillsRepository(db: DefaultDB)(implicit ec: ExecutionContext) extends SkillsRepository {

  private val col = db.collection("skills")
  private implicit val writer = Macros.writer[Skill]
  private implicit val reader = Macros.reader[Skill]

  override def saveSkill(skill: Skill):Future[Unit] = {
    col.insert(skill) flatMap { w => Future.unit }
  }

  override def deleteSkill(name: String): Future[Unit] = {
    col.findAndRemove(BSONDocument("_id" -> name)).map(_=>())
  }

  override def getSkills(): Future[Seq[Skill]] = {
      col.find[BSONDocument](BSONDocument.empty).cursor[Skill]().collect[Seq](-1, (s: Seq[Skill], t: Throwable) => Cont(s))
    }

  override def updateSkill(name: String, skillLevel: Int): Future[Unit] = {
    val updateOp = col.updateModifier(
      BSONDocument("$set" -> BSONDocument("skillLevel" -> skillLevel)))
    col.findAndModify(BSONDocument("_id" -> name), updateOp).map(_ => ())
  }

  override def addTag(name: String, tag: String): Future[Boolean] =  addDeleteTag(true)(name, tag)

  override def deleteTag(name: String, tag: String): Future[Boolean] = addDeleteTag(false)(name, tag)

  private def addDeleteTag(add: Boolean)( name: String, tag: String): Future[Boolean] = {
    val updateOp = col.updateModifier(
      BSONDocument((if(add) "$push" else "$pull") -> BSONDocument("tags" -> tag)))
    col.findAndModify(BSONDocument("_id" -> name), updateOp).map(r => r.value match {
      case None => false
      case Some(v) => v.elements.nonEmpty
    })
  }

}