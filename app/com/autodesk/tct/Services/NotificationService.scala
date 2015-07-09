package com.autodesk.tct.services

import com.notnoop.apns.APNS

/**
 * Handles notification service (right now it's APN)
 */
object NotificationService {

  /**
   * A list of devices that currently registered with notification service
   */
  val devices: collection.mutable.Map[String, String] = collection.mutable.Map()

  /**
   * Apple notification service
   */
  lazy val apnService = {
    val APNSCertStream = getClass.getResourceAsStream("/Certificates_Notification_Dev.p12")
    val APNSCertPassword = "tct2015@acrd"
    APNS.newService.withCert(APNSCertStream, APNSCertPassword).withSandboxDestination.build
  }

  /**
   * Save a new device node
   * @param userId user id
   * @param deviceToken devide token
   */
  def saveNewDevice(userId: String, deviceToken: String) = {
    devices.put(userId, deviceToken)
  }

  /**
   * Logout a current device
   * @param userId user id
   */
  def logoutDevice(userId: String) = {
    devices.remove(userId)
  }

  /**
   * Push a new event to every one
   * @param eventTitle event title
   * @param eventId event id
   * @param category event category
   */
  def newEventPush(eventTitle: String, eventId: String, category: String) = {
    val payload = APNS.newPayload.alertBody(s"New $category: $eventTitle").badge(1).sound("default")
      .customField("eventType", "new-event").customField("eventId", eventId).build
    devices.values.foreach {
      deviceToken =>
        val notif = apnService.push(deviceToken, payload)
        println(s"Result Received Is $notif")
    }
  }

  /**
   * Push events to selected users
   * @param eventTitle the event title
   * @param eventId the event id
   * @param category the event category
   * @param userIds the selected user ids
   */
  def userSelectedPush(eventTitle: String, eventId: String, category: String, userIds: Set[String]) = {
    val payload = APNS.newPayload.alertBody(s"You has been selected in $category: $eventTitle").badge(1).sound("default")
      .customField("eventType", "user-selected").customField("eventId", eventId).build
    userIds.foreach {
      userId => devices.get(userId).foreach {
        deviceToken =>
          val notif = apnService.push(deviceToken, payload)
          println(s"Result Received Is $notif")
      }
    }
  }
}
