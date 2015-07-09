package com.autodesk.tct.challenger.cassandra

import java.util.{Date, UUID}

import com.autodesk.tct.challenger.data.repositories.{EventExtension, IEventExtensionRepository}
import com.websudos.phantom.Implicits.{CassandraTable, _}

import scala.concurrent.Future

/**
 * EventExtension Table (Cassandra Table representation)
 */
sealed class EventExtensionTable extends CassandraTable[EventExtensionTable, EventExtension] {

  /**
   * The id of the event
   */
  object Id extends TimeUUIDColumn(this) with Index[UUID]

  /**
   * The presenter of the event
   */
  object Presenter extends StringColumn(this)

  /**
   * The presenter's picture
   */
  object PresenterLogo extends StringColumn(this)

  /**
   * The presenter's email
   */
  object PresenterEmail extends StringColumn(this)

  /**
   * The presenter's title
   */
  object PresenterTitle extends StringColumn(this)

  /**
   * The prerequisites of this event (if any)
   */
  object Prerequisites extends StringColumn(this)

  /**
   * Tags of this event
   */
  object Tags extends StringColumn(this)

  /**
   * Attachments of this event (if any)
   */
  object Attachments extends StringColumn(this)

  /**
   * Register policy, could be "First Come First Served", "Random" etc.
   */
  object RegisterPolicy extends StringColumn(this)

  /**
   * The collection of users that liked this event.
   */
  object LikeUsers extends SetColumn[EventExtensionTable, EventExtension, String](this)

  /**
   * The collection of users that registered (and is selected) this event
   */
  object RegisteredUsers extends MapColumn[EventExtensionTable, EventExtension, String, Date](this)

  /**
   * The collection of users that applied this event
   */
  object ApplyingUsers extends MapColumn[EventExtensionTable, EventExtension, String, Date](this)

  /**
   * The collection of users that followed this event.
   */
  object Followers extends SetColumn[EventExtensionTable, EventExtension, String](this)

  /**
   * Create an EventExtension from a row
   * @param row a row of data
   * @return an EventExtension object
   */
  def fromRow(row: Row): EventExtension = {
    EventExtension(
      Id(row),
      Presenter(row),
      PresenterLogo(row),
      PresenterEmail(row),
      PresenterTitle(row),
      Prerequisites(row),
      Tags(row),
      Attachments(row),
      RegisterPolicy(row),
      LikeUsers(row),
      RegisteredUsers(row),
      ApplyingUsers(row),
      Followers(row)
    )
  }
}

/**
 * Repository defines how the EventExtension could be interacted
 */
private object EventExtensionRepository extends EventExtensionTable with IEventExtensionRepository {

  /**
   * Cassandra table name
   */
  override val tableName = "EventExtension"

  implicit val session = CassandraClient.session

  /**
   * Add a new event extension
   * @param eventExtension an event extension entity object
   * @return Unit
   */
  def insertEventExtension(eventExtension: EventExtension): Future[Unit] = {
    this.insert.value(_.Id, eventExtension.id)
      .value(_.Presenter, eventExtension.presenter)
      .value(_.PresenterLogo, eventExtension.presenterLogo)
      .value(_.PresenterEmail, eventExtension.presenterEmail)
      .value(_.PresenterTitle, eventExtension.presenterTitle)
      .value(_.Prerequisites, eventExtension.prerequisites)
      .value(_.Tags, eventExtension.tags)
      .value(_.Attachments, eventExtension.attachments)
      .value(_.RegisterPolicy, eventExtension.registerPolicy)
      .value(_.LikeUsers, eventExtension.likeUsers)
      .value(_.RegisteredUsers, eventExtension.registeredUsers)
      .value(_.ApplyingUsers, eventExtension.applyingUsers)
      .value(_.Followers, eventExtension.followers)
      .future()
      .map { _ => }
  }

  /**
   * Get an event extension by id
   * @param id the event extension id
   * @return a future of a possible event extension
   */
  def getEventExtension(id: UUID): Future[Option[EventExtension]] = {
    this.select.where(_.Id eqs id).one
  }

  /**
   * Get event extensions by ids
   * @param ids the event extension list
   * @return a future of the possible sequence of event extensions
   */
  def getEventExtensions(ids: List[UUID]): Future[Seq[EventExtension]] = {
    this.select.where(_.Id in ids).fetch
  }

  /**
   * Get all event extension list
   * @return a future of the possible sequence of event extensions
   */
  def getAll: Future[Seq[EventExtension]] = {
    this.select.fetch
  }

  /**
   * Update an event extension
   * @param eventExtension the updating event extension
   * @return Unit
   */
  override def updateEventExtension(eventExtension: EventExtension): Future[Unit] = {
    this.update.where(_.Id eqs eventExtension.id)
      .modify(_.ApplyingUsers setTo eventExtension.applyingUsers)
      .and(_.Presenter setTo eventExtension.presenter)
      .and(_.PresenterLogo setTo eventExtension.presenterLogo)
      .and(_.PresenterEmail setTo eventExtension.presenterEmail)
      .and(_.PresenterTitle setTo eventExtension.presenterTitle)
      .and(_.Attachments setTo eventExtension.attachments)
      .and(_.RegisterPolicy setTo eventExtension.registerPolicy)
      .and(_.Followers setTo eventExtension.followers)
      .and(_.LikeUsers setTo eventExtension.likeUsers)
      .and(_.Prerequisites setTo eventExtension.prerequisites)
      .and(_.RegisteredUsers setTo eventExtension.registeredUsers)
      .and(_.Tags setTo eventExtension.tags)
      .future().map { _ => }
  }

  /**
   * add users to applying user sets
   * @param eventId the event id
   * @param userIds the userIds
   * @return Unit
   */
  override def addEventApplyingUsers(eventId: UUID, userIds: Map[String, Date]): Future[Unit] = {
    this.update.where(_.Id eqs eventId)
      .modify(_.ApplyingUsers.putAll(userIds))
      .future().map { _ => }
  }
}
