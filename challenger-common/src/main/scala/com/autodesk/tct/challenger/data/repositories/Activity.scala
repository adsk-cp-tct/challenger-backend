/*
 *  Copyright (c) 2015 by PROJECT Challenger
 *  All rights reserved.
 */
package com.autodesk.tct.challenger.data.repositories

import java.util.UUID

import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * Activity - records how entities interact with each other and form a timeline
 * @param id the id of this activity
 * @param subjectId the id of the activity subject
 * @param subject the human readable representation of the activity subject
 * @param verb the activity verb, must be one of [[com.autodesk.tct.challenger.common.CommonConstants.ActivityObjType]]
 * @param objectId the id of the activity object
 * @param obj the human readable representation of the activity object
 * @param objType the type of the activity object
 * @param data the human readable data represents this activity
 * @param createdTime activity created time
 * @param creator the human readable creator
 */
case class Activity(id: UUID,
                    subjectId: UUID,
                    subject: String = "",
                    verb: String = "",
                    objectId: UUID,
                    obj: String = "",
                    objType: String = "",
                    data: String = "",
                    createdTime: DateTime = DateTime.now(),
                    creator: String = "")

/**
 * Interface - defines how Activity could be interacted
 */
trait IActivityRepository {

  /**
   * Add a new activity
   * @param activity an activity entity object
   * @return a future indicates whether the insertion is successful or not
   */
  def insertActivity(activity: Activity): Future[Boolean]

  /**
   * Get an activity by id
   * @param id the activity id
   * @return a future of the possible activity
   */
  def getActivity(id: UUID): Future[Option[Activity]]

  /**
   * Get a list of activities given the limit
   * @param limit if limit > 0 return limit records, otherwise return all records
   * @return  the future of the possible activity sequence
   */
  def getAll(limit: Int): Future[Seq[Activity]]

  /**
   * Get a list of activities given the limit
   * @param limit if limit > 0 return limit records, otherwise return all records
   * @param start the start date time
   * @param end the end date time
   * @return the future of the possible activity sequence
   */
  def getAll(limit: Int, start: DateTime, end: DateTime): Future[Seq[Activity]]

}
