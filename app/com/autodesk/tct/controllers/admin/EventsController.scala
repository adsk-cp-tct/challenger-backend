package com.autodesk.tct.controllers.admin

import java.util.UUID

import com.autodesk.tct.controllers.BaseAuthController
import com.autodesk.tct.services.{EventService, IdeaService, UserService}
import com.autodesk.tct.share.Constants
import com.autodesk.tct.utilities.AsJson
import com.autodesk.tct.views
import play.api.{Logger, Play}
import play.api.mvc.{Action, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
 * Event controller for Admin
 */
object EventsController extends BaseAuthController {
  /**
   * The default events list page
   */
  def index = Action { request =>
    (request.session.get("user"), request.session.get("password")) match {
      case (Some(user), Some(pwd)) =>
        if (UserService.adminSignin(user, pwd).isEmpty) {
          Redirect("/").withNewSession
        } else {
          val eventList = Await.result(EventService.getEventList, 5.seconds)
          val ideaList = Await.result(IdeaService.getIdeaList(-1), 5.seconds)

          Ok(views.html.event(eventList.toString())(ideaList.toString()))
        }
      case _ => Redirect("/").withNewSession
    }
  }

  /**
   * Renders the event details page
   *
   * @param eventId the event ID
   */
  def getEvent(eventId: String) = Action { request =>
    (request.session.get("user"), request.session.get("password")) match {
      case (Some(user), Some(pwd)) =>
        if (UserService.adminSignin(user, pwd).isEmpty) {
          Redirect("/").withNewSession
        } else {
          Ok(views.html.eventDetails(eventId))
        }
      case _ => Redirect("/").withNewSession
    }
  }

  /**
   * Revokes an event.
   *
   * @param eventId the event ID
   * @return "Success" or the error message
   */
  def deleteEvent(eventId: String): Action[String] = Action.async(parse.tolerantText) { request =>
    (request.session.get("user"), request.session.get("password")) match {
      case (Some(user), Some(pwd)) =>
        if (UserService.adminSignin(user, pwd).isEmpty) {
          Future {
            Unauthorized.withHeaders(
              Constants.HeaderKey.TroubleShooting -> "only admin can remove event"
            )
          }
        } else {
          EventService.deleteEvent(UUID.fromString(eventId)).map {
            case true => Results.Ok(AsJson(Map("status" -> "success"))).as(Constants.ContentType.Json)
            case false => Results.InternalServerError.withHeaders(
              Constants.HeaderKey.TroubleShooting -> "delete event fails!"
            )
          }
        }
      case _ => Future {
        Unauthorized.withHeaders(
          Constants.HeaderKey.TroubleShooting -> "missing userName or password"
        )
      }
    }
  }
}