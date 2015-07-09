/*
 *  Copyright (c) 2015 by PROJECT Challenger
 *  All rights reserved.
 */
package com.autodesk.tct.challenger.data.repositories

import java.util.UUID

import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * User - represent a user (colleague in autodesk)
 * @param id the id of the user
 * @param groupMembers reserved, specify groups to which the user belongs
 * @param realName real name of the user
 * @param nickName nick name (display name) of the user
 * @param email email address (must be within autodesk.com domain)
 * @param password password of this account
 * @param avatar avatar of the user
 * @param description self description
 * @param followers who followed this user
 * @param followUsers other users followed by this user
 * @param registeredEvents registered events (and qualified)
 * @param applyingEvents applied events
 * @param createdTime register time of the user
 * @param updatedTime the time whenever the user updated his/her profile
 * @param lastLoginTime the time user last login
 */
case class User(
                 id: UUID,
                 groupMembers: Set[String] = Set(),
                 realName: String = "",
                 nickName: String = "",
                 email: String = "",
                 password: String = "",
                 avatar: String = "",
                 description: String = "",
                 followers: Set[String] = Set(),
                 followUsers: Set[String] = Set(),
                 registeredEvents: Set[String] = Set(),
                 applyingEvents: Set[String] = Set(),
                 createdTime: DateTime = new DateTime(0),
                 updatedTime: DateTime = new DateTime(0),
                 lastLoginTime: DateTime = new DateTime(0)
                 )

/**
 * Interface - defines how the user could be interacted
 */
trait IUserRepository {
  /**
   * Add a new user
   * @param user an user entity object
   * @return Unit
   */
  def insertUser(user: User): Future[Unit]

  /**
   * Update a user info
   * @param user the user to be updated
   */
  def updateUser(user: User): Future[User]

  /**
   * Get an user by id
   * @param id the user id
   * @return a future contains the possible user
   */
  def getUser(id: UUID): Future[Option[User]]

  /**
   * Get user list by id
   * @param ids the user id list
   * @return a future contains the possible sequence of users
   */
  def getUsers(ids: List[UUID]): Future[Seq[User]]

  /**
   * Get an user by email
   * @param email the email address
   * @return a future of the possible user
   */
  def getUserByEmail(email: String): Future[Option[User]]

  /**
   * Get an user by email and password
   * @param email the email
   * @param password the password
   * @return a future of the possible user
   */
  def getUserByEmailAndPassword(email: String, password: String): Future[Option[User]]
}
