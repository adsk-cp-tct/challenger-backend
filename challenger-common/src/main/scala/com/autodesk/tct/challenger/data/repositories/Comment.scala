/*
 *  Copyright (c) 2015 by PROJECT Challenger
 *  All rights reserved.
 */
package com.autodesk.tct.challenger.data.repositories

import java.util.UUID

import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Comment - comment could be on many types of object
 * @param id the id of this comment
 * @param targetId the id of the comment target
 * @param userId the user id that made this comment
 * @param content the content of the comment
 * @param createdTime the create time of the comment
 */
case class Comment(
                    id: UUID,
                    targetId: UUID,
                    userId: UUID,
                    content: String = "",
                    createdTime: DateTime = new DateTime(0)
                    )

/**
 * Interface - defines how Comment could be interacted
 */
trait ICommentRepository {
  /**
   * Add a new comment
   * @param comment a comment entity object
   * @return a future indicates whether the insertion is successful or not
   */
  def insertComment(comment: Comment): Future[Boolean]

  /**
   * Delete a comment
   * @param comment a comment object
   * @return a future indicates whether the deletion is successful or not
   */
  def deleteComment(comment: Comment): Future[Boolean]

  /**
   * Get comment by id
   * @param id the comment id
   * @return a future contains the possible comment
   */
  def getComment(id: UUID): Future[Option[Comment]]

  /**
   * Get comments by event id
   * @param targetId the event id
   * @return a future contains the possible comments list
   */
  def getCommentByTargetId(targetId: UUID): Future[Seq[Comment]]
}
