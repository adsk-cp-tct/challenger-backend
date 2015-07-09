/*
 *  Copyright (c) 2015 by PROJECT Challenger
 *  All rights reserved.
 */
package com.autodesk.tct.challenger.data.repositories

import java.util.UUID

import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Idea - Similar to Friend Group, everybody could post an idea an starts
 *  a discussion around this topic
 * @param id the id of the idea
 * @param title the title of the idea
 * @param description the description of the idea
 * @param thumbnails the thumbnail of the idea
 * @param followers the collection of users that follows this idea
 * @param likedUsers the collection of users that likes this idea
 * @param createdTime the create time of this idea
 * @param createdBy who created this idea
 * @param partition @deprecated it should not be used anymore.
 *                  it was used by cassandra sorting.
 */
case class Idea(
                  id: UUID,
                  title: String = "",
                  description: String = "",
                  thumbnails: Set[String] = Set(),
                  followers: Set[UUID] = Set(),
                  likedUsers: Set[UUID] = Set(),
                  createdTime: DateTime = DateTime.now(),
                  createdBy: UUID,
                  partition: String = "ip1"
                  )

/**
 * Interface - defines how idea could be interacted
 */
trait IIdeaRepository {

  /**
   * Insert an idea
   * @param idea the idea to be inserted
   * @return a future indicates whether the insertion is successful or not
   */
  def insertIdea(idea: Idea): Future[Boolean]

  /**
   * Get an idea by GUID
   * @param id the id of the idea
   * @return a future contains the possible idea
   */
  def getIdea(id: UUID): Future[Option[Idea]]

  /**
   * Get all ideas maximum by limit
   * @param limit the max number of ideas to get
   * @return a future contains the possible sequence of ideas
   */
  def getAll(limit: Int): Future[Seq[Idea]]

  /**
   * Get all ideas between 2 dates, maximum by limit
   * @param limit the max number of ideas to get
   * @param start the start date
   * @param end the end date
   * @return a future contains the possible sequence of ideas
   */
  def getAll(limit: Int, start: DateTime, end: DateTime): Future[Seq[Idea]]

  /**
   * Get ideas of specific user, maximum by limit
   * @param uid the id of the user
   * @param limit the max number of ideas to get
   * @return a future contains the possible sequence of ideas
   */
  def getByUid(uid: UUID, limit: Int): Future[Seq[Idea]]

  /**
   * Get ideas of specific user between 2 dates, maximum by limit
   * @param uid the id of the user
   * @param limit the max number of ideas to get
   * @param start the start date
   * @param end the end date
   * @return a future contains the possible sequence of ideas
   */
  def getByUid(uid: UUID, limit: Int, start: DateTime, end: DateTime): Future[Seq[Idea]]

  /**
   * Update an idea
   * @param idea the idea to be updated
   * @return a future indicates whether the update is successful or not
   */
  def updateIdea(idea: Idea): Future[Boolean]
}
