package com.autodesk.tct.controllers

import java.util.UUID

import com.autodesk.tct.challenger.common.CommonConstants
import com.autodesk.tct.services.{ActivityService, IdeaService}
import com.autodesk.tct.share.Constants
import com.autodesk.tct.utilities.AsJson
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object IdeaController extends Controller {


  /**
   * Post an idea
   *
   * @param Request data: list of events in formatted in json, sample is:
   *      {
   *              "title": "introduction to",
   *              "summary": "this is..",
   *              "thumbnail": "",
   *              "description": "this is...",
   *              "userId": "userId",
   *              "user": "userId"
   *       }
   * @return created idea or the error message
   */
  def createIdea = Action.async(parse.tolerantText) {
    req => {
      AsJson(req.body).asOpt[JsObject] match {
        case Some(obj) => IdeaService.createIdea(obj).map {
          case Some(e) => {
            val user = (obj \ "user").asOpt[String].getOrElse("someone")
            val userId = (obj \ "userId").asOpt[String].getOrElse("")

            ActivityService.createActivity(
              user, userId,
              Some(user),
              CommonConstants.ActivityVerb.CREATE,
              (e \ "id").asOpt[String].getOrElse(""),
              (e \ "title").asOpt[String].getOrElse(""),
              CommonConstants.ActivityObjType.IDEA)

            Results.Ok(e).as("application/json")
          }
          case _ => Results.InternalServerError("Failed to create the idea >_<!!!")
        }
        case _ => Future(Results.BadRequest("The input was not a valid JsObject"))
      }
    }
  }

  /**
   * Get an idea
   *
   * @param id the idea id
   * @return the idea or the error message
   */
  def getIdea(id: String) = Action.async(parse.tolerantText) {
    req => {
      IdeaService.getIdeaDetail(UUID.fromString(id)).map {
        case Some(idea) => {
          Results.Ok(idea)
        }
        case _ => Results.NotFound("Failed to found the idea >_<!!!")
      }
    }
  }

  /**
   * Get list of ideas that posted by a given user
   *
   * @param uidString the user id
   * @return list of ideas or the error message
   */
  def getIdeaListByUser(uidString: String) = Action.async(parse.tolerantText) {
    val uid = UUID.fromString(uidString)
    req => {
      val limit = req.getQueryString("limit")
      val start = req.getQueryString("start")
      val end = req.getQueryString("end")

      val future = (limit, start, end) match {
        case (Some(l), Some(s), Some(e)) => IdeaService.getIdeaListByUser(uid, l.toInt, DateTime.parse(s), DateTime.parse(e))
        case (Some(l), Some(s), _) => IdeaService.getIdeaListByUser(uid, l.toInt, DateTime.parse(s), DateTime.now())
        case (Some(l), _, _) => IdeaService.getIdeaListByUser(uid, l.toInt)
        case _ => IdeaService.getIdeaListByUser(uid, -1)
      }

      future.map {
        case ideas => {
          Results.Ok(ideas)
        }
      }
    }
  }

  /**
   * Get a list of ideas given the limit
   *
   * @param Request date (limit, start, end) in json format
   *       limit if limit > 0 return limit records, otherwise return all records
   *       start the start date time
   *       end the end date time
   *      {
   *              "limit": 10,
   *              "start": "2015-05-04T00:00:00.000+08:00",
   *              "end": "2015-05-04T00:00:00.000+08:00",
   *       }
   * @return list of idea or the error message
   */
  def getIdeaList = Action.async(parse.tolerantText) {
    req => {
      val limit = req.getQueryString("limit")
      val start = req.getQueryString("start")
      val end = req.getQueryString("end")

      val future = (limit, start, end) match {
        case (Some(l), Some(s), Some(e)) => IdeaService.getIdeaList(l.toInt, DateTime.parse(s), DateTime.parse(e))
        case (Some(l), Some(s), _) => IdeaService.getIdeaList(l.toInt, DateTime.parse(s), DateTime.now())
        case (Some(l), _, _) => IdeaService.getIdeaList(l.toInt)
        case _ => IdeaService.getIdeaList(-1)
      }

      future.map {
        case ideas => {
          Results.Ok(ideas)
        }
      }
    }
  }

  /**
   * Like an idea
   *
   * @param Request date (ideaId, likeUserId, title) in json format
   *      {
   *              "ideaId": "123",
   *              "likeUserId": "456",
   *              "title": "idea title",
   *       }
   * @return "Success" or the error message
   */
  def like(): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          (json \ "ideaId", json \ "likeUserId", json \ "title") match {
            case (JsString(ideaId), JsString(userId), JsString(title)) =>
              IdeaService.likeIdeaByUser(ideaId, UUID.fromString(userId)).map {
                r => {
                  ActivityService.createActivity(userId, userId, None, CommonConstants.ActivityVerb.LIKE, ideaId, title, CommonConstants.ActivityObjType.IDEA)
                  Results.Ok(AsJson(r))
                }
              }.recover {
                case e: Throwable =>
                  Logger.logger.error(e.getMessage, e)
                  Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "like event fails!")
              }
            case _ =>
              Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "missing param ideaId or likeUserId or title"))
          }

        case None =>
          Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }

  }

  /**
   * Unlike an idea
   *
   * @param ideaId the idea id
   * @param likeUserId the user id
   * @return "Success" or the error message
   */
  def unlike(ideaId: String, likeUserId: String): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      IdeaService.unlikeIdeaByUser(ideaId, UUID.fromString(likeUserId)).map {
        r => {
          Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
        }
      }.recover {
        case e: Throwable =>
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "unlike idea fails!")
      }
  }

  /**
   * Follow an idea
   *
   * @param Request date (ideaId, likeUserId, title) formatted in json
   *      {
   *              "ideaId": "123",
   *              "follower: "456",
   *              "title": "idea title",
   *       }
   * @return "Success" or the error message
   */
  def follow(): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          (json \ "ideaId", json \ "follower", json \ "title") match {
            case (JsString(ideaId), JsString(userId), JsString(title)) =>
              IdeaService.followIdeaByUser(ideaId, UUID.fromString(userId)).map {
                r => {
                  ActivityService.createActivity(userId, userId, None, CommonConstants.ActivityVerb.LIKE, ideaId, title, CommonConstants.ActivityObjType.IDEA)
                  Results.Ok(AsJson(r))
                }
              }.recover {
                case e: Throwable =>
                  Logger.logger.error(e.getMessage, e)
                  Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "like event fails!")
              }
            case _ =>
              Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "missing param ideaId or follower or title"))
          }

        case None =>
          Future(Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }

  }

  /**
   * Unfollow an idea
   *
   * @param ideaId the idea id
   * @param follower the user id
   * @return "Success" or the error message
   */
  def unfollow(ideaId: String, follower: String): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      IdeaService.unfollowIdeaByUser(ideaId, UUID.fromString(follower)).map {
        r => {
          Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
        }
      }.recover {
        case e: Throwable =>
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "unlike idea fails!")
      }
  }
}
