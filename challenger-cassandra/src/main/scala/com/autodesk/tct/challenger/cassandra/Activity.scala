package com.autodesk.tct.challenger.cassandra

import java.util.UUID

import com.autodesk.tct.challenger.data.repositories.{Activity, IActivityRepository}
import com.websudos.phantom.Implicits.{CassandraTable, _}
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Activity Table (Cassandra Table representation)
 */
sealed class ActivityRepositoryTable extends CassandraTable[ActivityRepositoryTable, Activity] {

  /**
   * Id of the user
   */
  object Id extends TimeUUIDColumn(this) with PrimaryKey[UUID]

  /**
   * Id of the activity subject
   */
  object SubjectId extends TimeUUIDColumn(this)

  /**
   * The human readable representation of the activity subject
   */
  object Subject extends StringColumn(this)

  /**
   * The activity verb, must be one of [[com.autodesk.tct.challenger.common.CommonConstants.ActivityObjType]]
   */
  object Verb extends StringColumn(this)

  /**
   * The id of the activity object
   */
  object ObjectId extends TimeUUIDColumn(this)

  /**
   * The human readable representation of the activity object
   */
  object Obj extends StringColumn(this)

  /**
   * The type of the activity object
   */
  object ObjType extends StringColumn(this)

  /**
   * The human readable data represents this activity
   */
  object Data extends StringColumn(this)

  /**
   * Activity created time
   */
  object CreatedTime extends DateTimeColumn(this) with ClusteringOrder[DateTime] with Descending

  /**
   * The human readable creator
   */
  object Creator extends StringColumn(this)

  /**
   * Create an activity from a row
   * @param row a row of data
   * @return an Activity object
   */
  def fromRow(row: Row): Activity = {
    Activity(
      Id(row),
      SubjectId(row),
      Subject(row),
      Verb(row),
      ObjectId(row),
      Obj(row),
      ObjType(row),
      Data(row),
      CreatedTime(row),
      Creator(row)
    )
  }
}

/**
 * Repository defines how the Activity could be interacted
 */
private object ActivityRepository extends ActivityRepositoryTable with IActivityRepository {

  /**
   * Cassandra table name
   */
  override val tableName = "Activity"

  implicit val session = CassandraClient.session

  /**
   * Add a new activity
   * @param activity an activity entity object
   * @return a future indicates whether the insertion is successful or not
   */
  def insertActivity(activity: Activity): Future[Boolean] = {
    this.insert
      .value(_.Id, activity.id)
      .value(_.SubjectId, activity.subjectId)
      .value(_.Subject, activity.subject)
      .value(_.Verb, activity.verb)
      .value(_.ObjectId, activity.objectId)
      .value(_.Obj, activity.obj)
      .value(_.ObjType, activity.objType)
      .value(_.Data, activity.data)
      .value(_.CreatedTime, activity.createdTime)
      .value(_.Creator, activity.creator)
      .future()
      .map {
      _.wasApplied()
    }
  }

  /**
   * Get an activity by id
   * @param id the activity id
   * @return a future of the possible activity
   */
  def getActivity(id: UUID): Future[Option[Activity]] = {
    this.select.where(_.Id eqs id).one
  }

  /**
   * Get a list of activities given the limit
   * @param limit if limit > 0 return limit records, otherwise return all records
   * @return  the future of the possible activity sequence
   */
  def getAll(limit: Int): Future[Seq[Activity]] = sort {
    val select = this.select
    limit match {
      case l if l > 0 => select.limit(l).fetch
      case _ => select.fetch
    }
  }

  /**
   * Get a list of activities given the limit
   * @param limit if limit > 0 return limit records, otherwise return all records
   * @param start the start date time
   * @param end the end date time
   * @return the future of the possible activity sequence
   */
  def getAll(limit: Int, start: DateTime, end: DateTime): Future[Seq[Activity]] = sort {
    val select = this.select
    val result = limit match {
      case l if l > 0 => select.limit(l)
      case _ => select
    }

    result.where(_.CreatedTime gte start)
      .and(_.CreatedTime lte end)
      .fetch
  }

  /**
   * Sort the activities
   * @param fseq the original sequence of activities
   * @return the sorted sequence of activities
   */
  def sort(fseq: Future[Seq[Activity]]): Future[Seq[Activity]] = fseq.map(
    _.sortWith(_.createdTime.getMillis > _.createdTime.getMillis)
  )
}
