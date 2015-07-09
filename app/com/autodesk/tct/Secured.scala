package com.autodesk.tct

import com.autodesk.tct.share.Constants
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Security Center - authenticate/authorize users
 */
trait Secured {

  /**
   * Retrieve the connected user id.
   */
  private def userId(request: RequestHeader): Option[String] = {
    request.session.get(Constants.SessionKey.UserId)
  }

  /**
   * Redirect to default page if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader): Result =
    Results.Redirect("/login").withNewSession

  /**
   * Action for authenticated users.
   */
  def isAuthenticated(bodyParser: BodyParser[AnyContent] = BodyParsers.parse.anyContent)
                     (f: => String => Request[AnyContent] => Result): EssentialAction =
    Security.Authenticated(userId, onUnauthorized) {
      userId =>
        Action.async(bodyParser)(request => Future(f(userId)(request)))
    }

  /**
   * Check whether user is authenticated
   * @param bodyParser the request body parser
   * @param f the future of the authenticated result
   */
  def isAuthenticatedWithFuture(bodyParser: BodyParser[AnyContent] = BodyParsers.parse.anyContent)(
    f: => String => Request[AnyContent] => Future[Result]): EssentialAction =
    Security.Authenticated(userId, onUnauthorized) {
      userId =>
        Action.async(bodyParser)(request => f(userId)(request))
    }
}
