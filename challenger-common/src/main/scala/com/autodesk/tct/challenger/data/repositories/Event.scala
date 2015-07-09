/*
 *  Copyright (c) 2015 by PROJECT Challenger
 *  All rights reserved.
 */
package com.autodesk.tct.challenger.data.repositories

import java.util.UUID

import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Event - TCT may have many types of events, this is the common base
 * @param id the id of the event
 * @param category the category of the event, e.g. brownbag, training, groupbuying etc.
 * @param title the title of the event
 * @param summary the summary of the event
 * @param expiration the expiration date of the event
 * @param thumbnail the thumbnail of the event
 * @param location where the event will be
 * @param startTime when the event will start
 * @param endTime when the event will end
 * @param costPerSeat how much does the event cost per seat
 * @param seats how many seats the event has in total
 * @param deliveryLanguage the event will be delivered in which language
 * @param description the description of the event
 * @param status the current status of the event
 * @param createdTime the create time of the event
 * @param createdBy who created the event
 * @param updatedTime the update time of the event (if any)
 * @param updatedBy who updated the event
 * @param partition @deprecated should not be used any more.
 *                  it was used by cassandra sorting.
 */
case class Event(
                  id: UUID,
                  category: String = "",
                  title: String = "",
                  summary: String = "",
                  expiration: DateTime = new DateTime(0),
                  thumbnail: String = "",
                  location: String = "",
                  startTime: DateTime,
                  endTime: DateTime,
                  costPerSeat: String = "",
                  seats: String = "",
                  deliveryLanguage: String = "",
                  description: String = "",
                  status: String = "",
                  createdTime: DateTime = DateTime.now(),
                  createdBy: String = "",
                  updatedTime: DateTime = DateTime.now(),
                  updatedBy: String = "",
                  partition: String = "ep1"
                  )

/**
 * Interface - defines how Event could be interacted
 */
trait IEventRepository {
  /**
   * Add a new event
   * @param event an event entity object
   * @return Unit
   */
  def insertEvent(event: Event): Future[Unit]

  /**
   * Get an event by event id
   * @param id event id
   * @return an event object
   */
  def getEvent(id: UUID): Future[Option[Event]]

  /**
   * Get events by event id list
   * @param ids event ids
   * @return an event list
   */
  def getEvents(ids: List[UUID]): Future[Seq[Event]]

  /**
   * Get all events list
   * @return all events list
   */
  def getAll: Future[Seq[Event]]

  /**
   * Delete a event
   * @param eventId event id
   * @return true if success
   */
  def deleteEvent(eventId: UUID): Future[Boolean]
}
