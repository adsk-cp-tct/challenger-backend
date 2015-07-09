package com.autodesk.tct.controllers

import com.autodesk.tct.Secured
import play.api.mvc._

import scala.concurrent.Future

class BaseAuthController extends Controller with Secured {
  /**
   * Secure action
   *
   * @param actionName action name
   * @param bodyParser body body parser function
   * @param thenn request handler function
   * @return
   */
  def securedAction(actionName: String,
                    bodyParser: BodyParser[AnyContent] = BodyParsers.parse.anyContent)
                   (thenn: (String, Request[AnyContent]) => Result): EssentialAction = {
    isAuthenticated(bodyParser) {
      userId =>
        request =>
          thenn(userId, request)

    }
  }

  /**
   * Secure action with future
   *
   * @param actionName action name
   * @param bodyParser request body parser function
   * @param thenn request handler function
   * @return
   */
  def securedActionWithFuture(actionName: String,
                              bodyParser: BodyParser[AnyContent] = BodyParsers.parse.anyContent)
                             (thenn: (String, Request[AnyContent]) => Future[Result]): EssentialAction = {
    isAuthenticatedWithFuture(bodyParser) {
      userId =>
        request =>
          thenn(userId, request)
    }
  }
}
