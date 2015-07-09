package com.autodesk.tct.models

/**
 * Event register information
 *
 * @param eventId the event id
 * @param registerUserIds the user ids of group members
 * @param groupId the user group id ,optional,
 * @param groupName the user group name , optional
 */
case class EventRegisterInfo(eventId: String, registerUserIds: List[String], groupId: Option[String], groupName: Option[String])

