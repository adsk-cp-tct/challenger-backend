package com.autodesk.tct.controllers

import com.autodesk.tct.models.ModelImplicit._
import com.autodesk.tct.models.SignUpUser
import com.autodesk.tct.services.UserService
import com.autodesk.tct.share.Constants
import com.autodesk.tct.utilities.AsJson
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AuthenticationController {

  /**
   * User sign in
   *
   * @param user account (email) and password, sample is
   * {
   *   "email":"yun.zhang@autodesk.com",
   *   "password":"111111"
   * }
   * @return user profile or the error message
   */
  def signin: Action[AnyContent] = Action.async(bodyParser = BodyParsers.parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          (json \ "email", json \ "password") match {
            case (JsString(email), JsString(password)) =>
              UserService.authenticate(email, password).map {
                case Some(u) => Results.Ok(Json.toJson(u)).as(Constants.ContentType.Json)
                case None => Results.Unauthorized.withHeaders(Constants.HeaderKey.TroubleShooting -> "email or password wrong!")
              }.recover {
                case e: Throwable =>
                  Logger.logger.error(e.getMessage, e)
                  Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> s"user $email signin fails!")
              }
            case _ => Future(Results.Unauthorized.withHeaders(Constants.HeaderKey.TroubleShooting -> "missing email or password"))
          }

        case None =>
          Future(Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }
  }

  /**
   * User sign up
   *
   * @param: user profile, sample is
   *  {
   *    "nickName":"Yun",
   *    "email":"yun.zhang@autodesk.com",
   *    "password":"111111"
   *  }
   * @return user profile or the error message
   */
  def signup(): Action[AnyContent] = Action.async(bodyParser = BodyParsers.parse.anyContent) {
    request =>
      request.body.asJson match {
        case Some(json) =>
          UserService.signUpUser(Json.fromJson[SignUpUser](json).get).map {
            r => Results.Ok(AsJson(r)).as(Constants.ContentType.Json)
          }.recover {
            case e: Throwable =>
              Logger.logger.error(e.getMessage, e)
              Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "sign up account fails!")
          }
        case None =>
          Future(Results.InternalServerError.withHeaders(Constants.HeaderKey.TroubleShooting -> "content should be json format!"))
      }
  }
}
