package com.autodesk.tct.challenger.cassandra

import java.util.UUID

import com.autodesk.tct.challenger.data.repositories.{Event, IEventRepository}
import com.websudos.phantom.Implicits.{CassandraTable, _}
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Event Table (Cassandra Table representation)
 */
sealed class EventRepositoryTable extends CassandraTable[EventRepositoryTable, Event] {

  /**
   * The id of the event
   */
  object Id extends TimeUUIDColumn(this) with PrimaryKey[UUID]

  /**
   * The category of the event, e.g. brownbag, training, groupbuying etc.
   */
  object Category extends StringColumn(this) with Index[String]

  /**
   * The title of the event
   */
  object Title extends StringColumn(this)

  /**
   * The summary of the event
   */
  object Summary extends StringColumn(this)

  /**
   * The expiration date of the event
   */
  object Expiration extends DateTimeColumn(this)

  /**
   * The thumbnail of the event
   */
  object Thumbnail extends StringColumn(this)

  /**
   * Where the event will be
   */
  object Location extends StringColumn(this)

  /**
   * When the event will start
   */
  object StartTime extends DateTimeColumn(this)

  /**
   * When the event will end
   */
  object EndTime extends DateTimeColumn(this)

  /**
   * How much does the event cost per seat
   */
  object CostPerSeat extends StringColumn(this)

  /**
   * How many seats the event has in total
   */
  object Seats extends StringColumn(this)

  /**
   * The event will be delivered in which language
   */
  object DeliveryLanguage extends StringColumn(this)

  /**
   * The description of the event
   */
  object Description extends StringColumn(this)

  /**
   * The current status of the event
   */
  object Status extends StringColumn(this)

  /**
   * The create time of the event
   */
  object CreatedTime extends DateTimeColumn(this) with Index[DateTime]

  /**
   * Who created the event
   */
  object CreatedBy extends StringColumn(this)

  /**
   * The update time of the event (if any)
   */
  object UpdatedTime extends DateTimeColumn(this)

  /**
   * Who updated the event
   */
  object UpdatedBy extends StringColumn(this)

  /**
   * @deprecated should not be used any more. It was used by cassandra sorting.
   */
  object Partition extends StringColumn(this)

  /**
   * Create an Event from a row
   * @param row a row of data
   * @return an Event object
   */
  def fromRow(row: Row): Event = {
    Event(
      Id(row),
      Category(row),
      Title(row),
      Summary(row),
      Expiration(row),
      Thumbnail(row),
      Location(row),
      StartTime(row),
      EndTime(row),
      CostPerSeat(row),
      Seats(row),
      DeliveryLanguage(row),
      Description(row),
      Status(row),
      CreatedTime(row),
      CreatedBy(row),
      UpdatedTime(row),
      UpdatedBy(row),
      Partition(row)
    )
  }
}

/**
 * Repository defines how the Event could be interacted
 */
private object EventRepository extends EventRepositoryTable with IEventRepository {

  /**
   * Cassandra table name
   */
  override val tableName = "Event"

  implicit val session = CassandraClient.session

  /**
   * Add a new event
   * @param event an event entity object
   * @return Unit
   */
  def insertEvent(event: Event): Future[Unit] = {
    this.insert.value(_.Id, event.id)
      .value(_.Category, event.category)
      .value(_.Title, event.title)
      .value(_.Summary, event.summary)
      .value(_.Expiration, event.expiration)
      .value(_.Thumbnail, event.thumbnail)
      .value(_.Location, event.location)
      .value(_.StartTime, event.startTime)
      .value(_.EndTime, event.endTime)
      .value(_.CostPerSeat, event.costPerSeat)
      .value(_.Seats, event.seats)
      .value(_.DeliveryLanguage, event.deliveryLanguage)
      .value(_.Description, event.description)
      .value(_.Status, event.status)
      .value(_.CreatedTime, event.createdTime)
      .value(_.CreatedBy, event.createdBy)
      .value(_.UpdatedTime, event.updatedTime)
      .value(_.UpdatedBy, event.updatedBy)
      .value(_.Partition, event.partition)
      .future()
      .map { _ => }
  }

  /**
   * Get an event by event id
   * @param id event id
   * @return an event object
   */
  def getEvent(id: UUID): Future[Option[Event]] = {
    this.select.where(_.Id eqs id).one
  }

  /**
   * Get events by event id list
   * @param ids event ids
   * @return an event list
   */
  def getEvents(ids: List[UUID]): Future[Seq[Event]] = sort {
    this.select.where(_.Id in ids).fetch
  }

  /**
   * Get all events list
   * @return all events list
   */
  def getAll: Future[Seq[Event]] = sort {
    this.select.fetch
  }

  /**
   * Delete a event
   * @param eventId event id
   * @return true if success
   */
  def deleteEvent(eventId: UUID): Future[Boolean] = {
    this.delete
      .where(_.Id eqs eventId)
      .future()
      .map { _.wasApplied() }
  }

  /**
   * Sort the events
   * @param fseq the original sequence of events
   * @return the sorted sequence of events
   */
  def sort(fseq: Future[Seq[Event]]): Future[Seq[Event]] = fseq.map {
    _.sortWith(_.createdTime.getMillis > _.createdTime.getMillis)
  }
}
