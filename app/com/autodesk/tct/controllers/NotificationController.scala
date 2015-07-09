package com.autodesk.tct.controllers

import com.autodesk.tct.services.NotificationService
import com.autodesk.tct.utilities.AsJson
import play.api.libs.json.JsObject
import play.api.mvc.BodyParsers.parse
import play.api.mvc.{Action, AnyContent, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object NotificationController {

  /**
   * Post a notification
   *
   * @param Request data (deviceToken, userId) formatted in json
   * @return "Success" or the error message
   */
  def post = Action.async(parse.tolerantText) {
    req => {
      AsJson(req.body).asOpt[JsObject] match {
        case Some(obj) => (obj \ "deviceToken").asOpt[String] match {
          case Some(deviceToken) =>
            (obj \ "userId").asOpt[String] match {
              case Some(userId) =>
                NotificationService.saveNewDevice(userId, deviceToken)
                Future(Results.Ok("ok"))
              case _ => Future(Results.BadRequest("The input did not contain userId"))
            }
          case _ => Future(Results.BadRequest("The input did not contain deviceToken"))
        }
        case _ => Future(Results.BadRequest("The input was not a valid JsObject"))
      }
    }
  }

  /**
   * Remove a device from notification list
   *
   * @param userId the user id
   * @return "Success" or the error message
   */
  def delete(userId: String): Action[AnyContent] = Action.async(parse.anyContent) {
    request =>
      NotificationService.logoutDevice(userId)
      Future(Results.Ok("ok"))
  }
}
