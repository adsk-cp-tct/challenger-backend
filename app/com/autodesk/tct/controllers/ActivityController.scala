package com.autodesk.tct.controllers

import com.autodesk.tct.services.ActivityService
import org.joda.time.DateTime
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object ActivityController extends Controller {

  /**
   * Get a list of activities given the limit
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
   * @return list of activities or the error message
   */
  def getActivityList = Action.async(parse.tolerantText) {
    req => {
      val limit = req.getQueryString("limit")
      val start = req.getQueryString("start")
      val end = req.getQueryString("end")

      val future = (limit, start, end) match {
        case (Some(l), Some(s), Some(e)) => ActivityService.getActivityList(l.toInt, DateTime.parse(s), DateTime.parse(e))
        case (Some(l), Some(s), _) => ActivityService.getActivityList(l.toInt, DateTime.parse(s), DateTime.now())
        case (Some(l), _, _) => ActivityService.getActivityList(l.toInt)
        case _ => ActivityService.getActivityList(-1)
      }

      future.map {
        case ideas => Results.Ok(ideas)
      }
    }
  }
}
