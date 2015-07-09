package com.autodesk.tct.services

import java.util.UUID

import com.autodesk.tct.challenger.data.repositories.{Activity, RepositoryFactory}
import com.autodesk.tct.models.tools._
import org.apache.cassandra.utils.UUIDGen
import org.joda.time.DateTime
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ActivityService {

  /**
   * Create an activity
   *
   * @param creator the creator id
   * @param subjectId subject id
   * @param subject an option of subject
   * @param verb the verb
   * @param objectId the object id
   * @param obj the object title
   * @param objType the object type
   * @param data the date string
   * @return the future of the possible created activity
   */
  def createActivity(creator: String,
                     subjectId: String,
                     subject: Option[String],
                     verb: String,
                     objectId: String,
                     obj: String,
                     objType: String,
                     data: String = ""): Future[Option[JsValue]] = {

    val uid = UUID.fromString(subjectId)
    val userName = {
      subject match {
        case None =>
          RepositoryFactory.factory.userRepository.getUser(uid).map(_.get.realName)
        case Some(sbj) =>
          Future(sbj)
      }
    }

    userName.flatMap {
      name => {
        val activity = Activity(
          id = UUIDGen.getTimeUUID,
          subjectId = uid,
          subject = name,
          verb = verb,
          objectId = UUID.fromString(objectId),
          obj = obj,
          objType = objType,
          creator = creator
        )

        lazy val res = ModelConverter.toActivity(activity)

        val insertF = RepositoryFactory.factory.activityRepository.insertActivity(activity)
        insertF.map {
          case true => Some(res)
          case false => None
        }
      }
    }
  }

  /**
   * Create an activity from a given formatted json
   *
   * @param obj input json
   * @return the future of the possible created activity
   */
  def createActivity(obj: JsObject): Future[Option[JsValue]] = {

    val activity = JsonConverter.toActivity(obj)

    lazy val res = ModelConverter.toActivity(activity)

    val insertF = RepositoryFactory.factory.activityRepository.insertActivity(activity)
    insertF.map {
      case true => Some(res)
      case false => None
    }
  }

  /**
   * Get a list of activities given the limit
   * @param limit if limit > 0 return limit records, otherwise return all records
   * @return  the future of the possible activity sequence
   */
  def getActivityList(limit: Int): Future[JsValue] = {
    val future = RepositoryFactory.factory.activityRepository.getAll(limit)
    future.map(ModelConverter.toActivityList)
  }

  /**
   * Get a list of activities given the limit
   * @param limit if limit > 0 return limit records, otherwise return all records
   * @param start the start date time
   * @param end the end date time
   * @return the future of the possible activity sequence
   */
  def getActivityList(limit: Int, start: DateTime, end: DateTime): Future[JsValue] = {
    val future = RepositoryFactory.factory.activityRepository.getAll(limit, start, end)
    future.map(ModelConverter.toActivityList)
  }
}
