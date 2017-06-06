package com.marktranter.skills

import cats.data.OptionT
import play.api.libs.iteratee.Iteratee

import scala.concurrent.{ExecutionContext, Future}
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver, QueryOpts}
import reactivemongo.api.collections.bson.BSONCollectionProducer
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, Macros, document}
import reactivemongo.play.iteratees.cursorProducer


/**
  * Created by mark on 29/05/17.
  */

object MongoEventBusListener {

  val driver = new MongoDriver

  def init(mongoUri: String, eventDispatcher: EventDispatcher)(implicit ec: ExecutionContext): Future[MongoEventBusListener] = for {
    uri <- Future.fromTry(MongoConnection.parseURI(mongoUri))
    con = driver.connection(uri)
    dn <- Future(uri.db.get)
    db <- con.database(dn)
  } yield new MongoEventBusListener(db,eventDispatcher)
}


class MongoEventBusListener(conn: DefaultDB,eventDispatcher: EventDispatcher)(implicit ec: ExecutionContext) {


  implicit object EventReader extends BSONDocumentReader[Option[DomainEvent]] {
  OptionT
    override def read(bson: BSONDocument): Option[DomainEvent] = bson.getAs[String]("EventType") match {
      case Some("ExperienceSkillAdded") => construct(ExperienceSkillAdded, bson)
      case Some("ExperienceSkillRemoved") => construct(ExperienceSkillRemoved, bson)
      case _ => None
    }

    private def construct[T <: DomainEvent](t: (String, String) => T, bson: BSONDocument): Option[T] = {
      bson.get("Event") match {
        case Some(doc) => doc match {
          case e: BSONDocument => {
            val co = e.getAs[String]("CompanyName")
            val skill = e.getAs[String]("Skill")
            co flatMap { c => skill map { t(c, _) }}
          }
          case _ => None
        }
        case _ => None
      }
    }
  }

  class EventStreamListener(eventStream: String) {
    private val col = conn.collection(eventStream).find(BSONDocument.empty).options(QueryOpts().tailable.awaitData).cursor[Option[DomainEvent]]().enumerator()
    val processEvents: Iteratee[Option[DomainEvent], Unit] =
      Iteratee.foreach { event =>
        event map eventDispatcher.dispatch
      }
    col.run(processEvents)
  }
}
