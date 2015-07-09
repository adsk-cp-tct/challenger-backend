package com.autodesk.tct.controllers.admin

import com.autodesk.tct.controllers.BaseAuthController
import com.autodesk.tct.services.UserService
import com.autodesk.tct.views
import play.api.mvc.{Action, AnyContent}

/**
 * The controller for Admin related operations
 */
object AdminController extends BaseAuthController {

  /**
   * The default login UI
   */
  def index = Action {
    _ => Ok(views.html.login(false)(false)(false))
  }

  /**
   * The user login page
   */
  def login = Action(parse.tolerantFormUrlEncoded) { request =>
    (request.body.get("username"), request.body.get("password")) match {
      case (Some(u), Some(p)) =>
        val (user, pwd) = (u.head, p.head)
        (user.nonEmpty, pwd.nonEmpty) match {
          case (true, true) =>
            val r = UserService.adminSignin(user, pwd)
            r.isEmpty match {
              case true => Ok(views.html.login(true)(true)(true))
              case false =>
                Redirect("/admin/events").withSession("user" -> user, "password" -> pwd)
            }

          case (u, p) => Ok(views.html.login(u)(p)(true))
        }

      case _ => Ok(views.html.login(false)(false)(true))
    }

  }

  def logout(): Action[AnyContent] = Action {
    Redirect("/login").withNewSession
  }
}
