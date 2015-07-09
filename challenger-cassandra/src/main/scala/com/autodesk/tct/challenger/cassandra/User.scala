package com.autodesk.tct.challenger.cassandra

import java.util.UUID

import com.autodesk.tct.challenger.data.repositories.{IUserRepository, User}
import com.websudos.phantom.Implicits.{CassandraTable, _}
import org.joda.time.DateTime

import scala.concurrent.Future

/**
 * User Table (Cassandra Table representation)
 */
sealed class UserRepositoryTable extends CassandraTable[UserRepositoryTable, User] {

  /**
   * The id of the user
   */
  object Id extends TimeUUIDColumn(this) with PrimaryKey[UUID]

  /**
   * Reserved, specify groups to which the user belongs
   */
  object GroupMembers extends SetColumn[UserRepositoryTable, User, String](this)

  /**
   * Real name of the user
   */
  object RealName extends StringColumn(this)

  /**
   * Nick name (display name) of the user
   */
  object NickName extends StringColumn(this)

  /**
   * Email address (must be within autodesk.com domain)
   */
  object Email extends StringColumn(this) with Index[String]

  /**
   * Password of this account
   */
  object Password extends StringColumn(this) with Index[String]

  /**
   * Avatar of the user
   */
  object Avatar extends StringColumn(this)

  /**
   * Self description
   */
  object Description extends StringColumn(this)

  /**
   * Who followed this user
   */
  object Followers extends SetColumn[UserRepositoryTable, User, String](this)

  /**
   * Other users followed by this user
   */
  object FollowUsers extends SetColumn[UserRepositoryTable, User, String](this)

  /**
   * Registered events (and qualified)
   */
  object RegisteredEvents extends SetColumn[UserRepositoryTable, User, String](this)

  /**
   * Applied events
   */
  object ApplyingEvents extends SetColumn[UserRepositoryTable, User, String](this)

  /**
   * Register time of the user
   */
  object CreatedTime extends DateTimeColumn(this)

  /**
   * The time whenever the user updated his/her profile
   */
  object UpdatedTime extends DateTimeColumn(this)

  /**
   * The time user last login
   */
  object LastLoginTime extends DateTimeColumn(this)

  /**
   * Create an User from a row
   * @param row a row of data
   * @return an User object
   */
  def fromRow(row: Row): User = {
    User(
      Id(row),
      GroupMembers(row),
      RealName(row),
      NickName(row),
      Email(row),
      Password(row),
      Avatar(row),
      Description(row),
      Followers(row),
      FollowUsers(row),
      RegisteredEvents(row),
      ApplyingEvents(row),
      CreatedTime(row),
      UpdatedTime(row),
      LastLoginTime(row)
    )
  }
}

/**
 * Repository defines how the User could be interacted
 */
private object UserRepository extends UserRepositoryTable with IUserRepository {

  /**
   * Cassandra table name
   */
  override val tableName = "User"

  implicit val session = CassandraClient.session

  /**
   * Add a new user
   * @param user an user entity object
   * @return Unit
   */
  def insertUser(user: User): Future[Unit] = {
    this.insert
      .value(_.Id, user.id)
      .value(_.GroupMembers, user.groupMembers)
      .value(_.RealName, user.realName)
      .value(_.NickName, user.nickName)
      .value(_.Email, user.email)
      .value(_.Password, user.password)
      .value(_.Avatar, user.avatar)
      .value(_.Description, user.description)
      .value(_.Followers, user.followers)
      .value(_.FollowUsers, user.followUsers)
      .value(_.RegisteredEvents, user.registeredEvents)
      .value(_.ApplyingEvents, user.applyingEvents)
      .value(_.CreatedTime, user.createdTime)
      .value(_.UpdatedTime, user.updatedTime)
      .value(_.LastLoginTime, user.lastLoginTime)
      .future()
      .map { _ => }
  }

  /**
   * Get an user by id
   * @param id the user id
   * @return a future contains the possible user
   */
  def getUser(id: UUID): Future[Option[User]] = {
    this.select.where(_.Id eqs id).one
  }

  /**
   * Get user list by id
   * @param ids the user id list
   * @return a future contains the possible sequence of users
   */
  def getUsers(ids: List[UUID]): Future[Seq[User]] = {
    this.select.where(_.Id in ids).fetch
  }

  /**
   * Get an user by email
   * @param email the email address
   * @return a future of the possible user
   */
  override def getUserByEmail(email: String): Future[Option[User]] = {
    this.select.where(_.Email eqs email).one()
  }

  /**
   * Get an user by email and password
   * @param email the email
   * @param password the password
   * @return a future of the possible user
   */
  override def getUserByEmailAndPassword(email: String, password: String): Future[Option[User]] = {
    this.select.allowFiltering().where(_.Email eqs email).and(_.Password eqs password).one()
  }

  /**
   * Update a user info
   * @param user the user to be updated
   */
  override def updateUser(user: User): Future[User] = {
    this.update.where(_.Id eqs user.id)
      .modify(_.Avatar setTo user.avatar)
      .and(_.Description setTo user.description)
      .and(_.Followers setTo user.followers)
      .and(_.FollowUsers setTo user.followUsers)
      .and(_.ApplyingEvents setTo user.applyingEvents)
      .and(_.GroupMembers setTo user.groupMembers)
      .and(_.LastLoginTime setTo user.lastLoginTime)
      .and(_.NickName setTo user.nickName)
      .and(_.RealName setTo user.realName)
      .and(_.RegisteredEvents setTo user.registeredEvents)
      .and(_.UpdatedTime setTo DateTime.now()
      ).future().map { _ => user}
  }
}
