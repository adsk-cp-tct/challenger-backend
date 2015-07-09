package com.autodesk.tct.controllers

import com.autodesk.tct.challenger.common.CommonConstants
import com.autodesk.tct.models.EventRegisterInfo
import com.autodesk.tct.models.ModelImplicit._
import com.autodesk.tct.services.{ActivityService, EventService}
import com.autodesk.tct.share.Constants
import com.autodesk.tct.utilities.AsJson
import play.api.Logger
import play.api.libs.json.{JsString, Json}
import play.api.mvc.BodyParsers.parse
import play.api.mvc.{Action, AnyContent, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object EventActionController {

  /**
   * Register a user to the given event.
   *  (1)update applyingUsers in table "event"
   *  (2)update registeredEvents in table "users"
   *  (3)add one activity
   *
   * @param Request data (eventId, userId) in json format.
   *       {
   *           "eventId": "123",
   *           "userId": "userA"
   *       }
   * @return "Success" or the error message
   */
  def registerEventByUser(): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          (json \ "eventId", json \ "userId") match {
            case (JsString(eventId), JsString(userId)) =>
              EventService.registerEventByUser(eventId, userId).map {
                r => Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
              }.recover {
                case e: Throwable =>
                  Logger.logger.error(e.getMessage, e)
                  Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "register event fails!")
              }
            case _ =>
              Future(Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "missing param eventId or userId"))
          }

        case None =>
          Future(Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }
  }

  /**
   * Register a group to the given event
   *  (1)create a virtual user in table "user"; update registeredEvents of all the real users in this group
   *  (2)update registeredEvents in table "users"
   *  (3)add one activity
   *
   * @param Request data (eventId, registerUserIds) in json format.
   *        {
   *            "eventId": "123",
   *            "registerUserIds": [
   *                "userA",
   *                "userB"
   *            ],
   *            "groupName": "groupName"
   *            "groupId":"groupId"
   *       }
   * @return "Success" or the error message
   */
  def registerEventByGroup(): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          EventService.registerEventByGroup(Json.fromJson[EventRegisterInfo](json).get).map {
            r => Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
          }.recover {
            case e: Throwable =>
              Logger.logger.error(e.getMessage, e)
              Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "register event fails!")
          }
        case None =>
          Future(Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }
  }

  /**
   * Unregister a user from the given event.
   *  (1)remove user from applyingUsers in table "event"
   *  (2)remove event from registeredEvents in table "users"
   *  (3)add one activity
   *
   * @param Request data (eventId, userId) in json format.
   *       {
   *           "eventId": "123",
   *           "userId": "userA"
   *       }
   * @return "Success" or the error message
   */
  def unregisterEvent(): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          (json \ "eventId", json \ "userId") match {
            case (JsString(eventId), JsString(userId)) =>
              EventService.unregisterEvent(eventId, userId).map {
                r => Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
              }.recover {
                case e: Throwable =>
                  Logger.logger.error(e.getMessage, e)
                  Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "unregister event fails!")
              }
            case _ =>
              Future(Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "missing param eventId or userId"))
          }

        case None =>
          Future(Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }

  }

  /**
   * Add one user who like the given event.
   *  (1)update likeUsers in table "event"
   *  (2)add one activity
   *
   * @param Request data (eventId, likeUserId) in json format.
   *      {
   *        "eventId": "123",
   *        "title": "An event",
   *        "likeUserId": "userA"
   *      }
   * @return "Success" or the error message
   */
  def likeEvent(): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          (json \ "eventId", json \ "likeUserId", json \ "title") match {
            case (JsString(eventId), JsString(userId), JsString(title)) =>
              EventService.likeEventByUser(eventId, userId).map {
                r => {
                  ActivityService.createActivity(userId, userId, None, CommonConstants.ActivityVerb.LIKE, eventId, title, CommonConstants.ActivityObjType.EVENT)
                  Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
                }
              }.recover {
                case e: Throwable =>
                  Logger.logger.error(e.getMessage, e)
                  Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "like event fails!")
              }
            case _ =>
              Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "missing param eventId or likeUserId or title"))
          }

        case None =>
          Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }

  }

  /**
   * Unlike an event
   *
   * @param eventId the event id
   * @param likeUserId the user id
   * @return "Success" or the error message
   */
  def unlikeEvent(eventId: String, likeUserId: String): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      EventService.unlikeEventByUser(eventId, likeUserId).map {
        r => {
          Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
        }
      }.recover {
        case e: Throwable =>
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "unlike event fails!")
      }

  }

  /**
   * Add one user who follow the given event.
   *  (1)update followers in table "event"
   *  (2)add one activity
   *
   * @param Request data (eventId, category, title, expiration, thumbnail, followUser) in json format.
   *      {
   *        "eventId": "123",
   *        "title": "An event",
   *        "follower": "userA"
   *      }
   * @return "Success" or the error message
  */
  def followEvent(): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          (json \ "eventId", json \ "follower", json \ "title") match {
            case (JsString(eventId), JsString(userId), JsString(title)) =>
              EventService.followEventByUser(eventId, userId).map {
                r => {
                  ActivityService.createActivity(userId, userId, None, CommonConstants.ActivityVerb.FOLLOW, eventId, title, CommonConstants.ActivityObjType.EVENT)
                  Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
                }
              }.recover {
                case e: Throwable =>
                  Logger.logger.error(e.getMessage, e)
                  Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "follow event fails!")
              }
            case _ =>
              Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "missing param eventId or follower or title"))
          }

        case None =>
          Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }

  }

  /**
   * Unfollow an event
   *
   * @param eventId the event id
   * @param follower the follower
   * @return "Success" or the error message
   */
  def unfollowEvent(eventId: String, follower: String): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      EventService.unfollowEventByUser(eventId, follower).map {
        r => Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
      }.recover {
        case e: Throwable =>
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "unfollow event fails!")
      }

  }

  /**
   * Add one user who follow the given user.
   *  (1)update followers in table "user"
   *  (2)add one activity
   *
   * @param Request data (eventId, category, title, expiration, thumbnail, followUser) in json format.
   *      {
   *        "user": "userB",
   *        "userName": "nameofuserB",
   *        "follower": "userA",
   *        "followerName": "nameofuserA"
   *      }
   * @return "Success" or the error message
   */
  def followUser(): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          (json \ "user", json \ "follower", json \ "userName", json \ "followerName") match {
            case (JsString(userId), JsString(follower), JsString(userName), JsString(followerName)) =>
              EventService.followUserByUser(userId, follower).map {
                r => {
                  ActivityService.createActivity(follower, follower, Some(followerName), CommonConstants.ActivityVerb.FOLLOW, userId, userName, CommonConstants.ActivityObjType.USER)
                  Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
                }
              }.recover {
                case e: Throwable =>
                  Logger.logger.error(e.getMessage, e)
                  Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "follow user fails!")
              }
            case _ =>
              Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "missing param userId or follower"))
          }

        case None =>
          Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }

  }

  /**
   * Get all the events that the given user has applying.
   *
   * @param userId the user id
   * @return list of event metadata or the error message
   */
  def getApplyingEvents(userId: String): Action[AnyContent] = Action.async {
    request =>
      EventService.getApplyingEvents(userId).map {
        events => Results.Ok(events)
      }.recover {
        case e: Throwable =>
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "get applying events fails!")
      }
  }

  /**
   * Get all the events that the given user has registered.
   *
   * @param userId the user id
   * @return list of event metadata or the error message
   */
  def getRegisteredEvents(userId: String): Action[AnyContent] = Action.async {
    request =>
      EventService.getRegisteredEvents(userId).map {
        events => Results.Ok(events)
      }.recover {
        case e: Throwable =>
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "get registered events fails!")
      }
  }

  /**
   * Tet all the contacts(who followed current user) for the given user.
   *
   * @param userId the user id
   * @return list of contacts or the error message
   */
  def getUserContacts(userId: String): Action[AnyContent] = Action.async {
    request =>
      EventService.getContacts(userId).map {
        contacts => Results.Ok(contacts)
      }.recover {
        case e: Throwable =>
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "get contacts fails!")
      }
  }

  /**
   * Update profile for the given user.
   *
   * @param userId the user id
   * @return user profile or the error message
   */
  def updateUserProfile(userId: String): Action[AnyContent] = Action.async {
    request =>
      request.body.asJson match {
        case Some(json) =>
          val (name, des, ava) = (json \ "nickName", json \ "description", json \ "avatar") match {
            case (JsString(nickName), JsString(description), JsString(avatar)) => (nickName, description, avatar)
            case (_, JsString(description), JsString(avatar)) => (null, description, avatar)
            case (JsString(nickName), _, JsString(avatar)) => (nickName, null, avatar)
            case (JsString(nickName), JsString(description), _) => (nickName, description, null)
            case (_, _, JsString(avatar)) => (null, null, avatar)
            case (_, JsString(description), _) => (null, description, null)
            case (JsString(nickName), _, _) => (nickName, null, null)
            case _ => (null, null, null)
          }

          EventService.updateUserProfile(userId, name, des, ava).map {
            user => Results.Ok(user).as("application/json")
          }.recover {
            case e: Throwable =>
              Logger.logger.error(e.getMessage, e)
              Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "update profile fails!")
          }
        case None => Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }

  }

  /**
   * Get profile for the given user.
   *
   * @param userId the user id
   * @return user profile or the error message
   */
  def getUserProfile(userId: String): Action[AnyContent] = Action.async {
    request =>
      EventService.getUserProfile(userId).map {
        case Some(user) => Results.Ok(user).as("application/json")
        case None => Results.NotFound
      }.recover {
        case e: Throwable =>
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "get user profile fails!")
      }
  }

  /**
   * Delete the specific follower of one user
   *  (1)update followers in table "user"
   *  (2)add one activity
   *
   * @param userId the user id
   * @param follower the follower id
   * @return "success" or the error message
   */
  def unfollowUser(userId: String, follower: String): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      EventService.unfollowUserByUser(userId, follower).map {
        r => Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
      }.recover {
        case e: Throwable =>
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "unfollow user fails!")
      }
  }

}
