package com.autodesk.tct.challenger.cassandra

import java.util.UUID

import com.autodesk.tct.challenger.data.repositories.{Idea, IIdeaRepository}

import com.websudos.phantom.Implicits.{CassandraTable, _}

import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Idea Table (Cassandra Table representation)
 */
sealed class IdeaTable extends CassandraTable[IdeaTable, Idea] {

  /**
   * The id of the idea
   */
  object Id extends TimeUUIDColumn(this) with PrimaryKey[UUID]

  /**
   * The title of the idea
   */
  object Title extends StringColumn(this)

  /**
   * The description of the idea
   */
  object Description extends StringColumn(this)

  /**
   * The thumbnail of the idea
   */
  object Thumbnails extends SetColumn[IdeaTable, Idea, String](this)

  /**
   * The collection of users that follows this idea
   */
  object Followers extends SetColumn[IdeaTable, Idea, UUID](this)

  /**
   * The collection of users that likes this idea
   */
  object LikedUsers extends SetColumn[IdeaTable, Idea, UUID](this)

  /**
   * The create time of this idea
   */
  object CreatedTime extends DateTimeColumn(this) with Index[DateTime]

  /**
   * Who created this idea
   */
  object CreatedBy extends TimeUUIDColumn(this) with Index[UUID]

  /**
   * @deprecated It should not be used anymore.It was used by cassandra sorting.
   */
  object Partition extends StringColumn(this)

  /**
   * Create an Idea from a row
   * @param row a row of data
   * @return an Idea object
   */
  def fromRow(row: Row): Idea = {
    Idea(
      Id(row),
      Title(row),
      Description(row),
      Thumbnails(row),
      Followers(row),
      LikedUsers(row),
      CreatedTime(row),
      CreatedBy(row),
      Partition(row)
    )
  }
}

/**
 * Repository defines how the Idea could be interacted
 */
private object IdeaRepository extends IdeaTable with IIdeaRepository {

  /**
   * Cassandra table name
   */
  override val tableName = "Idea"

  implicit val session = CassandraClient.session

  /**
   * Insert an idea
   * @param idea the idea to be inserted
   * @return a future indicates whether the insertion is successful or not
   */
  def insertIdea(idea: Idea): Future[Boolean] = {
    this.insert.value(_.Id, idea.id)
      .value(_.Title, idea.title)
      .value(_.Description, idea.description)
      .value(_.Thumbnails, idea.thumbnails)
      .value(_.Followers, idea.followers)
      .value(_.LikedUsers, idea.likedUsers)
      .value(_.CreatedTime, idea.createdTime)
      .value(_.CreatedBy, idea.createdBy)
      .value(_.Partition, idea.partition)
      .future()
      .map { _.wasApplied() }
  }

  /**
   * Get an idea by GUID
   * @param id the id of the idea
   * @return a future contains the possible idea
   */
  def getIdea(id: UUID): Future[Option[Idea]] = {
    this.select.where(_.Id eqs id).one
  }

  /**
   * Get all ideas maximum by limit
   * @param limit the max number of ideas to get
   * @return a future contains the possible sequence of ideas
   */
  def getAll(limit: Int): Future[Seq[Idea]] = sort {
    val select = this.select
    limit match {
      case l if l > 0 => select.limit(l).fetch
      case _ => select.fetch
    }
  }

  /**
   * Get all ideas between 2 dates, maximum by limit
   * @param limit the max number of ideas to get
   * @param start the start date
   * @param end the end date
   * @return a future contains the possible sequence of ideas
   */
  def getAll(limit: Int, start: DateTime, end: DateTime): Future[Seq[Idea]] = sort {
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
   * Get ideas of specific user, maximum by limit
   * @param uid the id of the user
   * @param limit the max number of ideas to get
   * @return a future contains the possible sequence of ideas
   */
  def getByUid(uid: UUID, limit: Int): Future[Seq[Idea]] = sort {
    val select = this.select.where(_.CreatedBy eqs uid)
    limit match {
      case l if l > 0 => select.limit(l).fetch
      case _ => select.fetch
    }
  }

  /**
   * Get ideas of specific user between 2 dates, maximum by limit
   * @param uid the id of the user
   * @param limit the max number of ideas to get
   * @param start the start date
   * @param end the end date
   * @return a future contains the possible sequence of ideas
   */
  def getByUid(uid: UUID, limit: Int, start: DateTime, end: DateTime): Future[Seq[Idea]] = sort {
    val select = this.select
    val result = limit match {
      case l if l > 0 => select.limit(l)
      case _ => select
    }

    result.where(_.CreatedBy eqs uid)
      .and(_.CreatedTime gte start)
      .and(_.CreatedTime lte end)
      .fetch
  }

  /**
   * Update an idea
   * @param idea the idea to be updated
   * @return a future indicates whether the update is successful or not
   */
  def updateIdea(idea: Idea): Future[Boolean] = {
    this.update.where(_.Id eqs idea.id)
      .modify(_.Title setTo idea.title)
      .and(_.Description setTo idea.description)
      .and(_.Thumbnails setTo idea.thumbnails)
      .and(_.Followers setTo idea.followers)
      .and(_.LikedUsers setTo idea.likedUsers)
      .future().map { _.wasApplied() }
  }

  /**
   * Sort the ideas
   * @param fseq the original sequence of ideas
   * @return the sorted sequence of ideas
   */
  def sort(fseq: Future[Seq[Idea]]): Future[Seq[Idea]] = fseq.map(
    _.sortWith(_.createdTime.getMillis > _.createdTime.getMillis)
  )
}
