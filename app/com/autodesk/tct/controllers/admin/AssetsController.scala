package com.autodesk.tct.controllers.admin

import controllers.AssetsBuilder
import play.api.mvc.{Action, AnyContent}

object AssetsController extends AssetsBuilder {
  def at(path: String, file: String, obj: Any): Action[AnyContent] = {
    at(path, file)
  }

  def index(path: String, file: String): Action[AnyContent] = {
    at(path, file)
  }

}
