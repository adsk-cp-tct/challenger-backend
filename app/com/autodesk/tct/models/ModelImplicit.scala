package com.autodesk.tct.models

import java.util.UUID

import play.api.libs.json._

object ModelImplicit {

  implicit object SignUpUserFormat extends Format[SignUpUser] {

    /**
     * Get SignUpUser for a given formatted json
     *
     * @param json input json
     * @return SignUpUser object
     */
    override def reads(json: JsValue): JsResult[SignUpUser] = JsSuccess(SignUpUser(
      (json \ "nickName").as[String],
      (json \ "email").as[String],
      (json \ "password").as[String]
    ))

    /**
     * Generate a formatted json for a given SignUpUser
     *
     * @param o the sign up user
     * @return a json value
     */
    override def writes(o: SignUpUser): JsValue = JsObject(List(
      "nickName" -> JsString(o.nickName),
      "email" -> JsString(o.email),
      "password" -> JsString(o.password)
    ))
  }

  implicit object SignInUserFormat extends Format[SignInUser] {

    /**
     * Get SignInUser for a given formatted json
     *
     * @param json input json
     * @return SignInUser object
     */
    override def reads(json: JsValue): JsResult[SignInUser] = JsSuccess(SignInUser(
      (json \ "id").as[UUID],
      (json \ "avatar").as[String],
      (json \ "nickName").as[String],
      (json \ "realName").as[String],
      (json \ "email").as[String]
    ))

    /**
     * Generate a formatted json for a given SignInUser
     *
     * @param o the sign in user
     * @return a json value
     */
    override def writes(o: SignInUser): JsValue = JsObject(List(
      "id" -> JsString(o.id.toString),
      "avatar" -> JsString(o.avatar),
      "nickName" -> JsString(o.nickName),
      "realName" -> JsString(o.realName),
      "email" -> JsString(o.email)
    ))
  }

  implicit object EventRegisterInfoFormat extends Format[EventRegisterInfo] {

    /**
     * Get EventRegisterInfo for a given formatted json
     *
     * @param json input json
     * @return EventRegisterInfo object
     */
    override def reads(json: JsValue): JsResult[EventRegisterInfo] = JsSuccess(EventRegisterInfo(
      (json \ "eventId").as[String],
      (json \ "registerUserIds").asOpt[List[String]].getOrElse(List()),
      (json \ "groupId").asOpt[String],
      (json \ "groupName").asOpt[String]
    ))

    /**
     * Generate a formatted json for a given EventRegisterInfo
     *
     * @param o the event register information
     * @return a json value
     */
    override def writes(o: EventRegisterInfo): JsValue = JsObject(List(
      "eventId" -> JsString(o.eventId),
      "groupId" -> JsString(o.groupId.getOrElse("")),
      "groupName" -> JsString(o.groupName.getOrElse("")),
      "registerUserIds" -> JsArray(o.registerUserIds.map(JsString))
    ))
  }

}
