package com.autodesk.tct.controllers

import com.autodesk.tct.challenger.common.CommonConstants
import com.autodesk.tct.challenger.common.CommonConstants.ActivityObjType.{EVENT, IDEA, USER}
import com.autodesk.tct.challenger.data.repositories.RepositoryFactory
import com.autodesk.tct.services.{ActivityService, CommentService}
import com.autodesk.tct.share.Constants
import com.autodesk.tct.utilities.AsJson
import play.api.Logger
import play.api.libs.json.JsString
import play.api.mvc.BodyParsers.parse
import play.api.mvc.{Action, AnyContent, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CommentController {

  /**
   * Add the comment message and the related user to the given event.
   *  (1)create a new comment in table "comment"
   *  (2)add one activity
   *
   * @param Request data (enventId, eventTitle, eventType, userId, comment) in json format.
   *      {
   *        "eventId": "123",
   *        "eventTitle": "introduction to",
   *        "eventType": "event",
   *        "userId": "userA",
   *        "comment": "blahblah"
   *      }
   * @return the created comment or the error message
   */
  def createComment(): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          (json \ "targetId", json \ "targetTitle", json \ "targetType", json \ "userId") match {
            case (JsString(targetId), JsString(title), typ@JsString(EVENT | IDEA | USER), JsString(userId)) =>
              CommentService.createComment(json).map {
                c => {
                  ActivityService.createActivity(userId, userId, None, CommonConstants.ActivityVerb.COMMENT, targetId, title, typ match { case JsString(t) => t })
                  Results.Ok(AsJson(c)).as(Constants.ContentType.Json)
                }
              }.recover {
                case e: Throwable =>
                  Logger.logger.error(e.getMessage, e)
                  Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "create comment fails!")
              }

            case _ => Future {
              val msg = "missing param targetId or targetTitle or targettType or userId"
              Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> msg)
            }
          }

        case None => Future {
          Results.BadRequest.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!")
        }
      }

  }

  /**
   * Remove a comment.
   *
   * @param commentId comment id
   * @return "Success" or the error message
  */
  def deleteComment(commentId: String): Action[AnyContent] = Action.async(parse.anyContent) {
    request => RepositoryFactory.factory.commentRepository.getComment(java.util.UUID.fromString(commentId)).flatMap {
      case Some(c) => CommentService.deleteComment(c).flatMap {
        case true => Future {
          Results.Ok(AsJson(Map("status" -> "success"))).as(Constants.ContentType.Json)
        }
        case false => Future {
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "delete comment fails!")
        }
      }.recover {
        case e: Throwable => {
          Logger.logger.error(e.getMessage, e)
          Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "delete comment fails!")
        }
      }
      case None => {
        val msg = "this comment is already deleted"
        Logger.logger.error(msg)
        Future {
          Results.Ok(AsJson(Map("status" -> "success", "message" -> msg))).as(Constants.ContentType.Json)
        }
      }
    }

  }
}
