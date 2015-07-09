package com.autodesk.tct.challenger.cassandra

import java.util.UUID

import com.autodesk.tct.challenger.data.repositories.{Comment, ICommentRepository}
import com.websudos.phantom.Implicits.{CassandraTable, _}
import com.websudos.phantom.query.QueryCondition

import scala.concurrent.Future

/**
 * Comment Table (Cassandra Table representation)
 */
sealed class CommentRepositoryTable extends CassandraTable[CommentRepositoryTable, Comment] {

  /**
   * The id of this comment
   */
  object Id extends TimeUUIDColumn(this) with PrimaryKey[UUID]

  /**
   * The id of the comment target
   */
  object EventId extends TimeUUIDColumn(this) with Index[UUID]

  /**
   * The user id that made this comment
   */
  object UserId extends TimeUUIDColumn(this)

  /**
   * The content of the comment
   */
  object Content extends StringColumn(this)

  /**
   * The create time of the comment
   */
  object CreatedTime extends DateTimeColumn(this)

  /**
   * Create a comment from a row
   * @param row a row of data
   * @return a Comment object
   */
  def fromRow(row: Row): Comment = {
    Comment(
      Id(row),
      EventId(row),
      UserId(row),
      Content(row),
      CreatedTime(row)
    )
  }
}

/**
 * Repository defines how the Comment could be interacted
 */
private object CommentRepository extends CommentRepositoryTable with ICommentRepository {

  /**
   * Cassandra table name
   */
  override val tableName = "Comment"

  implicit val session = CassandraClient.session

  /**
   * Add a new comment
   * @param comment a comment entity object
   * @return a future indicates whether the insertion is successful or not
   */
  def insertComment(comment: Comment): Future[Boolean] = {
    this.insert
      .value(_.Id, comment.id)
      .value(_.EventId, comment.targetId)
      .value(_.UserId, comment.userId)
      .value(_.Content, comment.content)
      .value(_.CreatedTime, comment.createdTime)
      .future()
      .map { _.wasApplied()}
  }

  /**
   * Delete a comment
   * @param comment a comment object
   * @return a future indicates whether the deletion is successful or not
   */
  def deleteComment(comment: Comment): Future[Boolean] = {
    this.delete
      .where(_.Id eqs comment.id)
      .and(_.EventId eqs comment.targetId)
      .future()
      .map { _.wasApplied() }
  }

  /**
   * Get comment by id
   * @param id the comment id
   * @return a future contains the possible comment
   */
  def getComment(id: UUID): Future[Option[Comment]] = {
    this.select.where(_.Id eqs id).one
  }

  /**
   * Get comments by event id
   * @param targetId the event id
   * @return a future contains the possible comments list
   */
  def getCommentByTargetId(targetId: UUID):Future[Seq[Comment]] = {
    this.select.where(_.EventId eqs targetId).fetch
  }

}
