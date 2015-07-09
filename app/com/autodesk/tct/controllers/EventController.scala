package com.autodesk.tct.controllers

import java.util.UUID

import com.autodesk.tct.challenger.common.CommonConstants
import com.autodesk.tct.services._
import com.autodesk.tct.utilities.AsJson
import play.api.libs.json.{JsObject, JsString, JsValue}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._

object EventController extends Controller {

  /**
   * For each of the event in the request data, first upload related thumbnails & attachments to server,
   *  then create an event in db, including tables: event, activity
   *
   * @param Request data: list of events in formatted in json, sample is:
   *      {
   *              "category": "training",
   *              "title": "introduction to",
   *              "summary": "this is..",
   *              "expiration": "2015-05-04T00:00:00.000+08:00",
   *              "thumbnail": "",
   *              "presenter": "huiting",
   *              "presenterLogo": "",
   *              "presenterIntroduction": "she is....",
   *              "location": "Shanghai Building 7 Room 3001",
   *              "startTime": "2015-05-01",
   *              "endTime": "2015-05-01",
   *              "costPerUser": "0",
   *              "seats": "30",
   *              "deliveryLanguage": "Chinese",
   *              "description": "this is...",
   *              "prerequisites": "no",
   *              "tags": "",
   *              "attachments": "",
   *              "status": "start",
   *              "user": "userId"
   *       }
   *  @return "Success" or the error message
   */
  def createEvent: Action[String] = Action.async(parse.tolerantText) {
    req => {
      AsJson(req.body).asOpt[JsObject] match {
        case Some(obj) => EventService.createEvent(obj).map {
          case Some(e) => {
            def getStr(v: JsValue) = v.asOpt[JsString] match {
              case Some(JsString(s)) => s
              case _ => ""
            }
            val uid = UserService.adminUserId
            ActivityService.createActivity(uid, uid, None, CommonConstants.ActivityVerb.CREATE, getStr(e \ "eventId"), getStr(e \ "title"), CommonConstants.ActivityObjType.EVENT)
            NotificationService.newEventPush(getStr(e \ "title"), getStr(e \ "eventId"), getStr(e \ "category"))
            Results.Ok(e).as("application/json")
          }
          case _ => Results.InternalServerError("Failed to create the event >_<!!!")
        }
        case _ => Future(Results.BadRequest("The input was not a valid JsObject"))
      }
    }
  }

  /**
   * This is only used to update detailed info of event itself. For each of the event values in the request data, update tables: event, activity
   *
   * @param Request data, list of event values to be updated in formatted in json, sample is:
   *      {
   *        "events": [
   *          {
   *            "eventId": "123",
   *            "status": "complete",
   *            "updater": "addf",        //consider whether this is available in session
   *            "updaterName": "xuchu",     //consider whether this is available in session
   *            "updaterAvatar": "/shjdfaa/avatar/21.png"  //consider whether this is available in session
   *          }
   *        ]
   *      }
   * @return "Success" or the error message
   */
  def updateEvents(): Unit = {

  }

  /**
   * Get the full event list. Each item contains: event basic info + like user count + registered user count.
   *  The query logic is:
   *   select eventId, category, title, validUntil, thumbnail, count(registeredUsers),
   *   count(likeUsers) from event group by eventId, category, title, validUntil, thumbnail
   *
   * !!Consider adding sorting/paging later!!
   *
   * @return list of event metadata(including list length) or the error message
 */
  def getEvents(): Action[AnyContent] = Action.async {
    EventService.getEventList.map(Results.Ok(_).as("application/json"))
  }

  /**
   * Get detailed info for the requested event.
   *
   * @param eventId the evnet id
   * @return detailed info for requested eventId or the error message
   */
  def getEvent(eventId: String): Action[AnyContent] = Action.async {
    EventService.getEventInDetail(UUID.fromString(eventId)).map(Results.Ok(_).as("application/json"))
  }
}
