package com.autodesk.tct.models.tools

import com.autodesk.tct.challenger.data.repositories._
import com.autodesk.tct.utilities._
import play.api.libs.json._

object ModelConverter {
  private def toComment(c: Comment, users: Set[User]): JsValue = {
    users.find(_.id == c.userId) match {
      case Some(u) => {
        AsJson(Map(
          "id" -> c.id.toString,
          "userId" -> u.id.toString,
          "userName" -> u.nickName,
          "userAvatar" -> u.avatar,
          "content" -> c.content,
          "createdTime" -> c.createdTime.toString()
        ))
      }
      case _ => JsNull
    }
  }

  /**
   * Generate a formatted json for a given event details
   *
   * @param fullEvent tuple of Event and EventExtension
   * @param cs list of comments
   * @param users user
   * @return a json value
   */
  def toEventDetails(fullEvent: (Event, EventExtension), cs: Seq[Comment], users: Set[User]): JsValue = {
    val (e, ext) = fullEvent
    AsJson(Map(
      "eventId" -> e.id.toString,
      "category" -> e.category,
      "title" -> e.title,
      "summary" -> e.summary,
      "createdTime" -> e.createdTime.toString(),
      "expiration" -> e.expiration.toString(),
      "thumbnail" -> e.thumbnail,
      "presenter" -> ext.presenter,
      "presenterLogo" -> ext.presenterLogo,
      "presenterEmail" -> ext.presenterEmail,
      "presenterTitle" -> ext.presenterTitle,
      "location" -> e.location,
      "startTime" -> e.startTime.toString(),
      "endTime" -> e.endTime.toString(),
      "costPerSeat" -> e.costPerSeat,
      "seats" -> e.seats,
      "deliveryLanguage" -> e.deliveryLanguage,
      "description" -> e.description,
      "registerPolicy" -> ext.registerPolicy,
      "prerequisites" -> ext.prerequisites,
      "tags" -> ext.tags,
      "attachments" -> ext.attachments,
      "status" -> e.status,
      "registerUsers" -> AsJson(ext.registeredUsers),
      "applyingUsers" -> {
        val regUsers = for {
          (uid, date) <- ext.applyingUsers.toSeq
          user <- users if user.id.toString == uid
        } yield user.id.toString -> user.avatar

        AsJson(regUsers.toMap)
      },
      "likeUsers" -> JsArray(ext.likeUsers.toSeq.map(JsString)), //Sequence of avatar if needs to display it in client
      "followers" -> JsArray(ext.followers.toSeq.map(JsString)),
      "comments" -> JsArray(cs.map(toComment(_, users)).filter(_.asOpt[JsObject].isDefined))
    ))
  }

  /**
   * Generate a formatted json for a given event metadata
   *
   * @param fullEvent tuple of Event and EventExtension
   * @return a json value
   */
  def toEventMetadata(fullEvent: (Event, EventExtension)): JsValue = {
    val (e, ext) = fullEvent
    AsJson(Map(
      "eventId" -> e.id.toString,
      "category" -> e.category,
      "title" -> e.title,
      // Use description as summary for event table for now
      "description" -> e.description,
      "createdTime" -> e.createdTime.toString(),
      "startTime" -> e.startTime.toString(),
      "endTime" -> e.endTime.toString(),
      "expiration" -> e.expiration.toString(),
      "thumbnail" -> e.thumbnail,
      "likeUserCount" -> ext.likeUsers.size,
      "seats" -> e.seats,
      "registerPolicy" -> ext.registerPolicy,
      "registeredUserCount" -> ext.registeredUsers.size,
      "applyingUserCount" -> ext.applyingUsers.size
    ))
  }

  /**
   * Generate a formatted json for a given list of events
   *
   * @param es list of tuple of Event and EventExtension
   * @return a json value
   */
  def toEventList(es: Seq[(Event, EventExtension)]): JsValue = {
    AsJson(Map(
      "total" -> es.length,
      "events" -> JsArray(es.map(toEventMetadata))
    ))
  }

  /**
   * Generate a formatted json for a given idea
   *
   * @param idea the idea object
   * @return a json value
   */
  def toIdeaMetadata(idea: Idea): JsValue = {
    AsJson(Map(
      "id" -> idea.id.toString,
      "title" -> idea.title,
      "description" -> idea.description,
      "thumbnails" -> idea.thumbnails,
      "followers" -> idea.followers,
      "followersCount" -> idea.followers.size,
      "likedUsers" -> idea.likedUsers,
      "likeUserCount" -> idea.likedUsers.size,
      "createdBy" -> idea.createdBy,
      "createdTime" -> idea.createdTime.toString()
    ))
  }

  /**
   * Generate a formatted json for a given idea detaila
   *
   * @param idea the idea object
   * @param cs list of comments
   * @param users list of users
   * @return a json value
   */
  def toIdeaDetails(idea: Idea, cs: Seq[Comment], users: Set[User]): JsValue = {
    val umap = users.map(u => u.id -> u).toMap
    val creator = umap.get(idea.createdBy)
    AsJson(Map(
      "id" -> idea.id.toString,
      "title" -> idea.title,
      "description" -> idea.description,
      "thumbnails" -> idea.thumbnails,
      "followers" -> idea.followers.filter(umap.contains).map {
        case uid => uid -> umap(uid).avatar
      }.toMap,
      "likedUsers" -> idea.likedUsers,
      "createdBy" -> idea.createdBy,
      "createdUser" -> creator.map(_.nickName).getOrElse(""),
      "createdAvatar" -> creator.map(_.avatar).getOrElse(""),
      "createdEmail" -> creator.map(_.email).getOrElse(""),
      "createdDescription" -> creator.map(_.description).getOrElse("This guy is lazy, didn't comment anything."),
      "createdTime" -> idea.createdTime.toString(),
      "comments" -> JsArray(cs.map(toComment(_, users)).filter(_.asOpt[JsObject].isDefined))
    ))
  }

  /**
   * Generate a formatted json for a given idea list
   *
   * @param ideas the list idea object
   * @return a json value
   */
  def toIdeaList(ideas: Seq[Idea]): JsValue = {
    AsJson(Map(
      "total" -> ideas.length,
      "ideas" -> JsArray(ideas.map(toIdeaMetadata))
    ))
  }

  /**
   * Generate a formatted json for a given list of idea json objects
   *
   * @param ideas the list of idea json object
   * @return a json value
   */
  def toIdeaJsList(ideas: Seq[JsValue]): JsValue = {
    AsJson(Map(
      "total" -> ideas.length,
      "ideas" -> JsArray(ideas)
    ))
  }

  /**
   * Generate a formatted json for a given activity
   *
   * @param activity the activity object
   * @return a json value
   */
  def toActivity(activity: Activity): JsValue = {
    AsJson(Map(
      "id" -> activity.id.toString,
      "subjectId" -> activity.subjectId.toString,
      "subject" -> activity.subject,
      "verb" -> activity.verb,
      "objectId" -> activity.objectId.toString,
      "obj" -> activity.obj,
      "objType" -> activity.objType,
      "data" -> activity.data,
      "createdTime" -> activity.createdTime,
      "creator" -> activity.creator
    ))
  }

  /**
   * Generate a formatted json for a given activity list
   *
   * @param activities the list of activity
   * @return a json value
   */
  def toActivityList(activities: Seq[Activity]): JsValue = {
    AsJson(Map(
      "total" -> activities.length,
      "activities" -> JsArray(activities.map(toActivity))
    ))
  }

  /**
   * Generate a formatted json for a user profile
   *
   * @param user the user object
   * @return a json value
   */
  def toUser(user: User): JsValue = {
    AsJson(Map(
      "id" -> user.id.toString,
      "groupMembers" -> JsArray(user.groupMembers.toSeq.map(JsString)),
      "realName" -> user.realName,
      "nickName" -> user.nickName,
      "email" -> user.email,
      "avatar" -> user.avatar,
      "description" -> user.description,
      "followers" -> JsArray(user.followers.toSeq.map(JsString)),
      "followUsers" -> JsArray(user.followUsers.toSeq.map(JsString)),
      "registeredEvents" -> JsArray(user.registeredEvents.toSeq.map(JsString)),
      "applyingEvents" -> JsArray(user.applyingEvents.toSeq.map(JsString)),
      "createdTime" -> user.createdTime.toString(),
      "updatedTime" -> user.updatedTime.toString(),
      "lastLoginTime" -> user.lastLoginTime.toString()
    ))
  }

  /**
   * Generate a formatted json for a list of contact
   *
   * @param contacts the list of contact
   * @return a json value
   */
  def toContactList(contacts: Seq[User]): JsValue = {
    AsJson(Map(
      "total" -> contacts.length,
      "contacts" -> JsArray(contacts.map(toUser))
    ))
  }

}
