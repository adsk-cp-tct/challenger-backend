/*
 *  Copyright (c) 2015 by PROJECT Challenger
 *  All rights reserved.
 */
package com.autodesk.tct.challenger.data.repositories

import java.util.{Date, UUID}

import org.joda.time.DateTime

import scala.collection.mutable
import scala.concurrent.Future

/**
 * EventExtension - more information describes the event
 * @param id the id of the event
 * @param presenter the presenter of the event
 * @param presenterLogo the presenter's picture
 * @param presenterEmail the presenter's email
 * @param presenterTitle the presenter's title
 * @param prerequisites the prerequisites of this event (if any)
 * @param tags tags of this event
 * @param attachments attachments of this event (if any)
 * @param registerPolicy register policy, could be "First Come First Served", "Random" etc.
 * @param likeUsers the collection of users that liked this event.
 * @param registeredUsers the collection of users that registered (and is selected) this event
 * @param applyingUsers the collection of users that applied this event
 * @param followers the collection of users that followed this event.
 */
case class EventExtension(
                           id: UUID,
                           presenter: String = "",
                           presenterLogo: String = "",
                           presenterEmail: String = "",
                           presenterTitle: String = "",
                           prerequisites: String = "",
                           tags: String = "",
                           attachments: String = "",
                           registerPolicy: String ="",
                           likeUsers: Set[String] = Set(),
                           registeredUsers: Map[String, Date] = Map(),
                           applyingUsers: Map[String, Date] = Map(),
                           followers: Set[String] = Set()
                           )

/**
 * Interface - defines how event extension could be interacted
 */
trait IEventExtensionRepository {
  /**
   * Add a new event extension
   * @param eventExtension an event extension entity object
   * @return Unit
   */
  def insertEventExtension(eventExtension: EventExtension): Future[Unit]

  /**
   * Update an event extension
   * @param eventExtension the updating event extension
   * @return Unit
   */
  def updateEventExtension(eventExtension: EventExtension): Future[Unit]

  /**
   * Add users to applying user sets
   * @param eventId the event id
   * @param userIds the userIds
   * @return Unit
   */
  def addEventApplyingUsers(eventId: UUID, userIds: Map[String, Date]): Future[Unit]

  /**
   * Get an event extension by id
   * @param id the event extension id
   * @return a future of a possible event extension
   */
  def getEventExtension(id: UUID): Future[Option[EventExtension]]

  /**
   * Get event extensions by ids
   * @param id the event extension list
   * @return a future of the possible sequence of event extensions
   */
  def getEventExtensions(id: List[UUID]): Future[Seq[EventExtension]]

  /**
   * Get all event extension list
   * @return a future of the possible sequence of event extensions
   */
  def getAll: Future[Seq[EventExtension]]
}
