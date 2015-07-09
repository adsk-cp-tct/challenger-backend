
package com.autodesk.tct.share

import java.util.Date

import akka.actor.Actor
import com.autodesk.tct.services.EventService
import org.joda.time.DateTime

/**
 * Akka message - call for applying policy
 */
case class ApplyPolicy()

/**
 * Actor - Applying pre-defined policy
 */
class ApplyRegistrationPolicy extends Actor {
  def receive = {
    case ApplyPolicy() => EventService.applyPolicyForAllEvent
  }
}

/**
 * The Register Policy object that implements different policies
 */
object ApplyRegistrationPolicy {

  /**
   * Apply policy for specific requirements
   * @param seats max number of seats
   * @param policy the policy name
   * @param applyingUsers currently applied users set
   * @return registered user set, a map of userId -> applyDate
   */
  def applyPolicy(seats: Int, policy: String, applyingUsers: Map[String, Date]): Map[String, Date] = policy match {
    case "Random" => {
      val selected = scala.util.Random.shuffle(applyingUsers.keySet).take(seats)
      applyingUsers.filterKeys(selected)
    }
    case "First Come First Served" => {
      applyingUsers.toSeq.sortBy(_._2).take(seats).toMap
    }
    case _ /*"more"*/ => applyingUsers.take(seats)
  }

  implicit def dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_ isBefore _)
}
