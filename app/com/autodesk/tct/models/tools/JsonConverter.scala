package com.autodesk.tct.models.tools

import java.util.UUID

import com.autodesk.tct.challenger.data.repositories._
import org.apache.cassandra.utils.UUIDGen
import org.joda.time.DateTime
import play.api.libs.json._

object JsonConverter {

  private def getString(obj: JsValue, defaultValue: String): String = {
    obj.asOpt[JsString] match {
      case Some(v) => v.value
      case _ => defaultValue
    }
  }

  private def getString(obj: JsValue): String = getString(obj, "")

  private def getStringSet(obj: JsValue, defaultValue: Set[String]): Set[String] = {
    obj.asOpt[Seq[String]] match {
      case Some(v) => v.toSet
      case _ => defaultValue
    }
  }

  private def getStringSet(obj: JsValue): Set[String] = getStringSet(obj, Set())

  private def getUUIDSet(obj: JsValue, defaultValue: Set[UUID]): Set[UUID] = {
    obj.asOpt[Seq[String]] match {
      case Some(v) => v.toSet.map(UUID.fromString)
      case _ => defaultValue
    }
  }

  private def getUUIDSet(obj: JsValue): Set[UUID] = getUUIDSet(obj, Set())

  private def getDateTime(obj: JsValue): DateTime = {
    val default = new DateTime(0)
    val dt = getString(obj, default.toString())
    try {
      DateTime.parse(dt)
    } catch {
      case _: Throwable => default
    }
  }

  private def getNewId = UUIDGen.getTimeUUID

  /**
   * Generate a Event object from a given json
   *
   * @param a input json
   * @return tuple of (Event, EvenExtension)
   */
  def toFullEvent(a: JsObject): (Event, EventExtension) = {
    val id = getNewId

    val event = Event(
      id = id,
      category = getString(a \ "category"),
      title = getString(a \ "title"),
      summary = getString(a \ "summary"),
      expiration = getDateTime(a \ "expiration"),
      thumbnail = getString(a \ "thumbnail"),
      location = getString(a \ "location"),
      startTime = getDateTime(a \ "startTime"),
      endTime = getDateTime(a \ "endTime"),
      costPerSeat = getString(a \ "costPerSeat"),
      seats = getString(a \ "seats"),
      deliveryLanguage = getString(a \ "deliveryLanguage"),
      description = getString(a \ "description"),
      status = getString(a \ "status"),
      updatedTime = DateTime.now(),
      updatedBy = getString(a \ "user")
    )

    val eventExt = EventExtension(
      id = id,
      presenter = getString(a \ "presenter"),
      presenterLogo = getString(a \ "presenterLogo"),
      presenterEmail = getString(a \ "presenterEmail"),
      presenterTitle = getString(a \ "presenterTitle"),
      prerequisites = getString(a \ "prerequisites"),
      tags = getString(a \ "tags"),
      attachments = getString(a \ "attachments"),
      registerPolicy = getString(a \ "registerPolicy")
    )

    (event, eventExt)
  }

  /**
   * Generate an idea object from a given json
   *
   * @param a input json
   * @return an idea object
   */
  def toIdea(a: JsObject): Idea = {

    val id = getNewId

    Idea(
      id = id,
      title = getString(a \ "title"),
      description = getString(a \ "description"),
      thumbnails = getStringSet(a \ "thumbnails"),
      followers = getUUIDSet(a \ "followers"),
      likedUsers = getUUIDSet(a \ "likedUsers"),
      createdTime = DateTime.now(),
      createdBy = UUID.fromString(getString(a \ "userId"))
    )
  }

  /**
   * Generate an activity object from a given json
   *
   * @param a input json
   * @return an activity object
   */
  def toActivity(a: JsObject): Activity = {

    val id = getNewId

    Activity(
      id = id,
      subject = getString(a \ "subject"),
      subjectId = UUID.fromString((a \ "subjectId").toString()),
      verb = getString(a \ "verb"),
      objectId = UUID.fromString((a \ "subjectId").toString()),
      obj = getString(a \ "obj"),
      objType = getString(a \ "objType"),
      data = getString(a \ "data"),
      createdTime = DateTime.now,
      creator = getString(a \ "creator")
    )
  }

  /**
   * Generate a comment object from a given json
   *
   * @param a input json
   * @return a comment object
   */
  def toComment(a: JsValue): Comment = {

    val id = getNewId

    Comment(
      id = id,
      targetId = java.util.UUID.fromString(getString(a \ "targetId")),
      userId = java.util.UUID.fromString(getString(a \ "userId")),
      content = getString(a \ "comment"),
      createdTime = DateTime.now
    )
  }

}
