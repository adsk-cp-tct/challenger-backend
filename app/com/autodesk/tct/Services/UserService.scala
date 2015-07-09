package com.autodesk.tct.services

import java.util.UUID

import com.autodesk.tct.challenger.data.repositories.{RepositoryFactory, User}
import com.autodesk.tct.models.{SignInUser, SignUpUser}
import com.autodesk.tct.share.Utils
import org.apache.cassandra.utils.UUIDGen
import org.joda.time.DateTime
import play.Play

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Handle User related operations
 */
object UserService {

  lazy val adminAccounts = Play.application().configuration().getStringList("application.admin.accounts").asScala.map(parseToAccount)
  lazy val adminUserId = Play.application().configuration().getString("application.admin.uuid")
  lazy val adminUserName = Play.application().configuration().getString("application.admin.name")

  /**
   * Parse input string into account
   * @param account the account string, separated by ":"
   * @return a tuple represents name -> password
   */
  private def parseToAccount(account: String): (String, String) = {
    val uAndp = account.split(":")
    (uAndp.head, uAndp.last)
  }

  /**
   * Sign in the admin
   * @param userName user name
   * @param password user password
   * @return sign in result, a highly customized key value store
   */
  def adminSignin(userName: String, password: String): Map[String, String] = {
    adminAccounts.count { case (u, p) => u == userName && p == password } match {
      case x if x >= 1 => Map("status" -> "success")
      case _ => Map.empty
    }
  }

  /**
   * Sign up a user
   * @param u the user info to sign up
   * @return a future of highly customized key value store
   */
  def signUpUser(u: SignUpUser): Future[Map[String, String]] = {
    for {
      existed <- RepositoryFactory.factory.userRepository.getUserByEmail(u.email)
      r <- existed match {
        case Some(user) => Future(Map("status" -> "fails", "message" -> s"email ${u.email} has been registered!"))
        case None =>
          RepositoryFactory.factory.userRepository.insertUser(User(
            id = UUIDGen.getTimeUUID,
            groupMembers = Set.empty,
            nickName = u.nickName,
            email = u.email,
            password = Utils.md5(u.password),
            followers = Set.empty,
            followUsers = Set.empty,
            registeredEvents = Set.empty,
            createdTime = DateTime.now(),
            updatedTime = DateTime.now(),
            lastLoginTime = DateTime.now()
          )).map {
            unit => Map("status" -> "success")
          }
      }
    } yield r
  }

  /**
   * Authenticate user using email and password
   * @param email user email
   * @param password user password
   * @return a future of possible signed in user
   */
  def authenticate(email: String, password: String): Future[Option[SignInUser]] = {
    RepositoryFactory.factory.userRepository.getUserByEmailAndPassword(email, Utils.md5(password)).map {
      case Some(u) => Some(SignInUser(u.id, u.avatar, u.nickName, u.realName, u.email))
      case None => None
    }
  }

  /**
   * Create default admin accounts
   * @return a future of highly customized key value store
   */
  def createAdmin: Future[Map[String, String]] = {
    RepositoryFactory.factory.userRepository.insertUser(User(
      id = UUID.fromString(adminUserId),
      groupMembers = Set.empty,
      nickName = adminUserName,
      email = "",
      password = "",
      followers = Set.empty,
      followUsers = Set.empty,
      registeredEvents = Set.empty,
      createdTime = DateTime.now(),
      updatedTime = DateTime.now(),
      lastLoginTime = DateTime.now()
    )).map {
      unit => Map("status" -> "success")
    }
  }
}
