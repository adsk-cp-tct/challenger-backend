package com.autodesk.tct.services

import java.util.{Date, UUID}

import com.autodesk.tct.challenger.data.repositories.{Event, EventExtension, RepositoryFactory, User}
import com.autodesk.tct.models.EventRegisterInfo
import com.autodesk.tct.models.tools._
import com.autodesk.tct.share.ApplyRegistrationPolicy
import org.apache.cassandra.utils.UUIDGen
import org.joda.time.DateTime
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Object handles event related operations
 */
object EventService {

  /**
   * Create an event
   * @param obj the input json object
   * @return a future of the possible created event
   */
  def createEvent(obj: JsObject): Future[Some[JsValue]] = {
    val (e, ext) = JsonConverter.toFullEvent(obj)

    lazy val res = ModelConverter.toEventDetails((e, ext), Seq(), Set())
    for {
      r1 <- RepositoryFactory.factory.eventRepository.insertEvent(e)
      r2 <- RepositoryFactory.factory.eventExtRepository.insertEventExtension(ext)
    } yield Some(res)
  }

  /**
   * Get event detail itself
   * @param id event id
   * @return a future of possible event details
   */
  def getEventInDetail(id: UUID): Future[JsValue] = {
    getFullEvent(id).flatMap {
      case Some(e) => getDetails(e)
      case _ => Future(JsNull)
    }
  }

  /**
   * Get a list of events
   * @return a future of possible events list
   */
  def getEventList: Future[JsValue] = {
    for {
      es <- RepositoryFactory.factory.eventRepository.getAll
      exts <- RepositoryFactory.factory.eventExtRepository.getAll
      res = combineEventSeqs(es, exts)
    } yield ModelConverter.toEventList(res)
  }

  /**
   * Delete an event
   * @param eventId event id
   * @return a future indicates whether deletion is successful
   */
  def deleteEvent(eventId: UUID): Future[Boolean] = RepositoryFactory.factory.eventRepository.deleteEvent(eventId)

  /**
   * Register an event by a user
   * @param eventId event id
   * @param userId user id
   * @return a future of highly customized key value store
   */
  def registerEventByUser(eventId: String, userId: String): Future[Map[String, String]] = {
    registerEvent(eventId, Map(userId -> new Date()))
  }

  /**
   * Like an event by a user
   * @param eventId event id
   * @param userId user id
   * @return a future of highly customized key value store
   */
  def likeEventByUser(eventId: String, userId: String): Future[Map[String, String]] = {
    likeEvent(eventId, List(userId))
  }

  /**
   * Unlike an event by a user
   * @param eventId event id
   * @param userId user id
   * @return a future of highly customized key value store
   */

  def unlikeEventByUser(eventId: String, userId: String): Future[Map[String, String]] = {
    unlikeEvent(eventId, List(userId))
  }

  /**
   * Follow an event by a user
   * @param eventId event id
   * @param userId user id
   * @return a future of highly customized key value store
   */
  def followEventByUser(eventId: String, userId: String): Future[Map[String, String]] = {
    followEvent(eventId, List(userId))
  }

  /**
   * Unfollow an event by a user
   * @param eventId event id
   * @param userId user id
   * @return a future of highly customized key value store
   */
  def unfollowEventByUser(eventId: String, userId: String): Future[Map[String, String]] = {
    unfollowEvent(eventId, List(userId))
  }

  /**
   * Apply policy for all pending events
   * @return a future indicates whether the applying process is successful
   */
  def applyPolicyForAllEvent: Future[Boolean] = {
    RepositoryFactory.factory.eventRepository.getAll.map {
      es => es.filter { e => (e.expiration.compareTo(DateTime.now()) < 0) }.map { e => applyPolicyToEvent(e.seats.toInt, e.id) }.forall(_ == Future {
        true
      })
    }
  }

  /**
   * Follow a user by a user
   * @param userId user id
   * @param follower follower id
   * @return a future of highly customized key value store
   */
  def followUserByUser(userId: String, follower: String): Future[Map[String, String]] = {
    val result = for {
      follow <- followUser(userId, List(follower))
      upFollow <- updateFollower(List(userId), follower)
    } yield follow && upFollow

    result.map {
      case true => Map("status" -> "success")
      case false => Map("status" -> "fails", "message" -> "current user or its follower does not existed")
    }
  }

  /**
   * Unfollow a user by a user
   * @param userId user id
   * @param follower follower id
   * @return a future of highly customized key value store
   */
  def unfollowUserByUser(userId: String, follower: String): Future[Map[String, String]] = {
    val result = for {
      unfollow <- unfollowUser(userId, List(follower))
      upUnfollow <- updateUnfollower(List(userId), follower)
    } yield unfollow && upUnfollow

    result.map {
      case true => Map("status" -> "success")
      case false => Map("status" -> "fails", "message" -> "current user or its follower does not existed")
    }
  }

  /**
   * Get contacts of specific user
   * @param userId user id
   * @return a future contains the possible users' contacts
   */
  def getContacts(userId: String): Future[JsValue] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(userId))
      contactList = user.map(_.followUsers.toList).getOrElse(List.empty)
      contacts <- RepositoryFactory.factory.userRepository.getUsers(contactList.map(UUID.fromString(_)))
    } yield ModelConverter.toContactList(contacts)
  }

  /**
   * Update user profile
   * @param id user id
   * @param name user name
   * @param des user description
   * @param ava user avatar
   * @return the updated user profile
   */
  def updateUserProfile(id: String, name: String, des: String, ava: String): Future[JsValue] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(id))
      newUser = user.map {
        u => User(
          id = UUID.fromString(id),
          nickName = (if (name.nonEmpty) name else u.nickName),
          description = (if (des.nonEmpty) des else u.description),
          avatar = (if (ava.nonEmpty) ava else u.avatar),
          groupMembers = u.groupMembers,
          applyingEvents = u.applyingEvents,
          realName = u.realName,
          email = u.email,
          password = u.password,
          followers = u.followers,
          followUsers = u.followUsers,
          registeredEvents = u.registeredEvents,
          createdTime = u.createdTime,
          updatedTime = DateTime.now(),
          lastLoginTime = u.lastLoginTime
        )
      }
      updateUser <- RepositoryFactory.factory.userRepository.updateUser(newUser.get)
    } yield ModelConverter.toUser(updateUser)
  }

  /**
   * Get user profile
   * @param userId user id
   * @return a future contains the possible user profile
   */
  def getUserProfile(userId: String): Future[Option[JsValue]] = {
    RepositoryFactory.factory.userRepository.getUser(UUID.fromString(userId)).map(_.map(ModelConverter.toUser(_)))
  }

  /**
   * Join event lists with id equal to each other
   * @param events the events to be joined
   * @param extensions the eventExt to be joined
   * @return the joined event list
   */
  private def joinEventLists(events: List[Event], extensions: List[EventExtension]): List[(Event, EventExtension)] = {
    events.groupBy(_.id).map {
      t => (t._2.head, extensions.groupBy(_.id).get(t._1).getOrElse(List.empty).headOption.getOrElse(new EventExtension(UUID.fromString(""))))
    }.toList
  }

  /**
   * Get applied events of specific user
   * @param userId user id
   * @return a future of the applied events
   */
  def getApplyingEvents(userId: String): Future[JsValue] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(userId))
      eventList = user.map(_.applyingEvents.toList).getOrElse(List.empty)
      events <- RepositoryFactory.factory.eventRepository.getEvents(eventList.map(UUID.fromString(_)))
      extensions <- RepositoryFactory.factory.eventExtRepository.getEventExtensions(eventList.map(UUID.fromString(_)))
    } yield ModelConverter.toEventList(joinEventLists(events.toList, extensions.toList))
  }

  /**
   * Get registered events of specific user
   * @param userId user id
   * @return a future of the registered events
   */
  def getRegisteredEvents(userId: String): Future[JsValue] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(userId))
      eventList = user.map(_.registeredEvents.toList).getOrElse(List.empty)
      events <- RepositoryFactory.factory.eventRepository.getEvents(eventList.map(UUID.fromString(_)))
      extensions <- RepositoryFactory.factory.eventExtRepository.getEventExtensions(eventList.map(UUID.fromString(_)))
    } yield ModelConverter.toEventList(joinEventLists(events.toList, extensions.toList))
  }

  /**
   * Only store groupId to applyingUsers of Event
   * @param eventRegisterInfo the register info
   * @return a future of highly customized key value store
   */
  def registerEventByGroup(eventRegisterInfo: EventRegisterInfo): Future[Map[String, String]] = {
    //should do validation fist,
    validate(eventRegisterInfo)
    eventRegisterInfo.groupId match {
      case Some(groupId) => updateGroupAndRegisterEvent(eventRegisterInfo.eventId, groupId, eventRegisterInfo.registerUserIds)
      case None => createGroupAndRegister(eventRegisterInfo)
    }
  }

  /**
   * Unregister from an event
   * @param eventId event id
   * @param userId user id
   * @return a future of highly customized key value store
   */
  def unregisterEvent(eventId: String, userId: String): Future[Map[String, String]] = {
    for {
      userUpdated <- removeEventFromUser(userId, eventId)
      eventUpdated <- removeUserFromEvent(userId, eventId).map {
        _ => Map("status" -> "success")
      }

    } yield eventUpdated
  }

  /**
   * Update group and register event
   * @param eventId event id
   * @param groupId group id
   * @param members a set of users belong to that group
   * @return a future of highly customized key value store
   */
  private def updateGroupAndRegisterEvent(eventId: String, groupId: String, members: List[String]): Future[Map[String, String]] = {
    for {
      group <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(groupId))
      groupUpdated <- group match {
        case Some(u) => RepositoryFactory.factory.userRepository.updateUser(User(id = u.id,
          groupMembers = u.groupMembers ++ members,
          realName = u.realName,
          nickName = u.nickName,
          email = u.email,
          password = u.password,
          avatar = u.avatar,
          description = u.description,
          followers = u.followers,
          followUsers = u.followUsers,
          registeredEvents = u.registeredEvents,
          applyingEvents = u.applyingEvents,
          updatedTime = DateTime.now()))
        case None => Future()
      }
      registered <- registerEvent(eventId, Map(groupId -> new Date()))

    } yield registered
  }

  /**
   * Create a group of people and register with event
   * @param info event register event
   * @return a future of highly customized key value store
   */
  private def createGroupAndRegister(info: EventRegisterInfo): Future[Map[String, String]] = {
    val userId = UUIDGen.getTimeUUID
    for {
      group <- RepositoryFactory.factory.userRepository.insertUser(User(id = userId,
        groupMembers = Set(info.registerUserIds: _*),
        realName = info.groupName.getOrElse(""),
        nickName = info.groupName.getOrElse("")
      ))
      registered <- registerEvent(info.eventId, Map(userId.toString -> new Date()))
    } yield registered
  }

  /**
   * Applying event to users
   * @param userId user id
   * @param eventId event id
   * @return Unit
   */
  private def addApplyingEventToUser(userId: String, eventId: String): Future[Unit] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(userId))
      r <- user match {
        case Some(u) => RepositoryFactory.factory.userRepository.updateUser(User(id = u.id,
          groupMembers = u.groupMembers,
          realName = u.realName,
          nickName = u.nickName,
          email = u.email,
          password = u.password,
          avatar = u.avatar,
          description = u.description,
          followers = u.followers,
          followUsers = u.followUsers,
          registeredEvents = u.registeredEvents,
          applyingEvents = u.applyingEvents + eventId,
          updatedTime = DateTime.now()))
        case None => Future()
      }

    } yield r
  }

  /**
   * Remove event from user
   * @param userId user id
   * @param eventId event id
   * @return Unit
   */
  private def removeEventFromUser(userId: String, eventId: String): Future[Unit] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(userId))
      r <- user match {
        case Some(u) => RepositoryFactory.factory.userRepository.updateUser(User(id = u.id,
          groupMembers = u.groupMembers,
          realName = u.realName,
          nickName = u.nickName,
          email = u.email,
          password = u.password,
          avatar = u.avatar,
          description = u.description,
          followers = u.followers,
          followUsers = u.followUsers,
          registeredEvents = if (u.registeredEvents.contains(eventId)) u.registeredEvents - eventId else u.registeredEvents,
          applyingEvents = if (u.applyingEvents.contains(eventId)) u.applyingEvents - eventId else u.applyingEvents,
          updatedTime = DateTime.now()))
        case None => Future()
      }

    } yield r
  }

  /**
   * Remove user from event
   * @param userId user id
   * @param eventId event id
   * @return Unit
   */
  private def removeUserFromEvent(userId: String, eventId: String): Future[Unit] = {
    for {
      event <- RepositoryFactory.factory.eventExtRepository.getEventExtension(UUID.fromString(eventId))
      r <- event match {
        case Some(e) => RepositoryFactory.factory.eventExtRepository.updateEventExtension(EventExtension(
          id = e.id,
          presenter = e.presenter,
          presenterLogo = e.presenterLogo,
          presenterEmail = e.presenterEmail,
          presenterTitle = e.presenterTitle,
          prerequisites = e.prerequisites,
          tags = e.tags,
          attachments = e.attachments,
          likeUsers = e.likeUsers,
          registeredUsers = if (e.registeredUsers.contains(userId)) e.registeredUsers - userId else e.registeredUsers,
          applyingUsers = if (e.applyingUsers.contains(userId)) e.applyingUsers - userId else e.applyingUsers,
          followers = e.followers))
        case None => Future()
      }

    } yield r
  }

  /**
   * register one user to a event
   * @param eventId the event id
   * @param userIds the user ids
   */
  private def registerEvent(eventId: String, userIds: Map[String, Date]): Future[Map[String, String]] = {
    for {
      event <- RepositoryFactory.factory.eventExtRepository.getEventExtension(UUID.fromString(eventId))
      r <- event match {
        case Some(e@EventExtension(id, presenter, presenterLogo, presenterEmail, presenterTitle, prerequisites, tags, attachments, registerPolicy, likeUsers, registeredUsers, applyingUsers, followers)) =>
          for {
            registered <- {
              val updatingEventExt = EventExtension(id,
                presenter,
                presenterLogo,
                presenterEmail,
                presenterTitle,
                prerequisites,
                tags,
                attachments,
                registerPolicy,
                likeUsers,
                registeredUsers,
                applyingUsers ++ userIds,
                followers)
              RepositoryFactory.factory.eventExtRepository.updateEventExtension(updatingEventExt)
            }
            userUpdated <- Future.sequence(userIds.map {
              case (u, t) => addApplyingEventToUser(u, eventId)
            }).map {
              _ => Map("status" -> "success")
            }
          } yield userUpdated
        case None => Future(Map("status" -> "fails", "message" -> "current event is not existed"))
      }
    } yield r
  }

  /**
   * A(more) user(s) likes this event
   * @param eventId the id of the event
   * @param userIds the user ids
   * @return
   */
  private def likeEvent(eventId: String, userIds: List[String]): Future[Map[String, String]] = {
    for {
      event <- RepositoryFactory.factory.eventExtRepository.getEventExtension(UUID.fromString(eventId))
      r <- event match {
        case Some(e@EventExtension(id, presenter, presenterLogo, presenterEmail, presenterTitle, prerequisites, tags, attachments, registerPolicy, likeUsers, registeredUsers, applyingUsers, followers)) => {
          val updatingEventExt = EventExtension(
            id,
            presenter,
            presenterLogo,
            presenterEmail,
            presenterTitle,
            prerequisites,
            tags,
            attachments,
            registerPolicy,
            (likeUsers ++ userIds).toList.distinct.toSet,
            registeredUsers,
            applyingUsers,
            followers)
          RepositoryFactory.factory.eventExtRepository.updateEventExtension(updatingEventExt).map { _ =>
            Map("status" -> "success")
          }
        }
        case None => Future(Map("status" -> "fails", "message" -> "current event is not existed"))
      }
    } yield r
  }

  /**
   * A(more) user(s) unlikes this event
   * @param eventId the id of the event
   * @param userIds the user ids
   * @return
   */
  private def unlikeEvent(eventId: String, userIds: List[String]): Future[Map[String, String]] = {
    for {
      event <- RepositoryFactory.factory.eventExtRepository.getEventExtension(UUID.fromString(eventId))
      r <- event match {
        case Some(e@EventExtension(id, presenter, presenterLogo, presenterEmail, presenterTitle, prerequisites, tags, attachments, registerPolicy, likeUsers, registeredUsers, applyingUsers, followers)) => {
          val updatingEventExt = EventExtension(
            id,
            presenter,
            presenterLogo,
            presenterEmail,
            presenterTitle,
            prerequisites,
            tags,
            attachments,
            registerPolicy,
            (likeUsers -- userIds).toList.distinct.toSet,
            registeredUsers,
            applyingUsers,
            followers)
          RepositoryFactory.factory.eventExtRepository.updateEventExtension(updatingEventExt).map { _ =>
            Map("status" -> "success")
          }
        }
        case None => Future(Map("status" -> "fails", "message" -> "current event is not existed"))
      }
    } yield r
  }

  /**
   * Follow an event
   * @param eventId the id of the event
   * @param userIds the follower
   * @return
   */
  private def followEvent(eventId: String, userIds: List[String]): Future[Map[String, String]] = {
    for {
      event <- RepositoryFactory.factory.eventExtRepository.getEventExtension(UUID.fromString(eventId))
      r <- event match {
        case Some(e@EventExtension(id, presenter, presenterLogo, presenterEmail, presenterTitle, prerequisites, tags, attachments, registerPolicy, likeUsers, registeredUsers, applyingUsers, followers)) => {
          val updatingEventExt = EventExtension(
            id,
            presenter,
            presenterLogo,
            presenterEmail,
            presenterTitle,
            prerequisites,
            tags,
            attachments,
            registerPolicy,
            likeUsers,
            registeredUsers,
            applyingUsers,
            (followers ++ userIds).toList.distinct.toSet)
          RepositoryFactory.factory.eventExtRepository.updateEventExtension(updatingEventExt).map { _ =>
            Map("status" -> "success")
          }
        }
        case None => Future(Map("status" -> "fails", "message" -> "current event is not existed"))
      }
    } yield r
  }

  /**
   * Unfollow an event
   * @param eventId the id of the event
   * @param userIds the follower
   * @return
   */
  private def unfollowEvent(eventId: String, userIds: List[String]): Future[Map[String, String]] = {
    for {
      event <- RepositoryFactory.factory.eventExtRepository.getEventExtension(UUID.fromString(eventId))
      r <- event match {
        case Some(e@EventExtension(id, presenter, presenterLogo, presenterEmail, presenterTitle, prerequisites, tags, attachments, registerPolicy, likeUsers, registeredUsers, applyingUsers, followers)) => {
          val updatingEventExt = EventExtension(id,
            presenter,
            presenterLogo,
            presenterEmail,
            presenterTitle,
            prerequisites,
            tags,
            attachments,
            registerPolicy,
            likeUsers,
            registeredUsers,
            applyingUsers,
            (followers -- userIds).toList.distinct.toSet)
          RepositoryFactory.factory.eventExtRepository.updateEventExtension(updatingEventExt).map { _ =>
            Map("status" -> "success")
          }
        }
        case None => Future(Map("status" -> "fails", "message" -> "current event is not existed"))
      }
    } yield r
  }

  /**
   * Follow a user
   * @param userId the id of the user
   * @param followers the followers
   * @return
   */
  private def followUser(userId: String, followers: List[String]): Future[Boolean] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(userId))
      r <- user match {
        case Some(u) => RepositoryFactory.factory.userRepository.updateUser(User(id = u.id,
          groupMembers = u.groupMembers,
          realName = u.realName,
          nickName = u.nickName,
          email = u.email,
          password = u.password,
          avatar = u.avatar,
          description = u.description,
          followers = (u.followers ++ followers).toList.distinct.toSet,
          followUsers = u.followUsers,
          registeredEvents = u.registeredEvents,
          applyingEvents = u.applyingEvents,
          updatedTime = DateTime.now())).map { _ => true }
        case None => Future(false)
      }
    } yield r
  }

  /**
   * Update followers list of the user
   * @param users users
   * @param follower follower
   * @return a future indicates whether the update is successful
   */
  private def updateFollower(users: List[String], follower: String): Future[Boolean] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(follower))
      r <- user match {
        case Some(u) => RepositoryFactory.factory.userRepository.updateUser(User(id = u.id,
          groupMembers = u.groupMembers,
          realName = u.realName,
          nickName = u.nickName,
          email = u.email,
          password = u.password,
          avatar = u.avatar,
          description = u.description,
          followers = u.followers,
          followUsers = (u.followUsers ++ users).toList.distinct.toSet,
          registeredEvents = u.registeredEvents,
          applyingEvents = u.applyingEvents,
          updatedTime = DateTime.now())).map { _ => true }
        case None => Future(false)
      }
    } yield r
  }

  /**
   * Unfollower a user
   * @param userId the user id
   * @param followers the followers
   * @return a future indicates whether the update is successful
   */
  private def unfollowUser(userId: String, followers: List[String]): Future[Boolean] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(userId))
      r <- user match {
        case Some(u) => RepositoryFactory.factory.userRepository.updateUser(User(id = u.id,
          groupMembers = u.groupMembers,
          realName = u.realName,
          nickName = u.nickName,
          email = u.email,
          password = u.password,
          avatar = u.avatar,
          description = u.description,
          followers = (u.followers -- followers).toList.distinct.toSet,
          followUsers = u.followUsers,
          registeredEvents = u.registeredEvents,
          applyingEvents = u.applyingEvents,
          updatedTime = DateTime.now())).map { _ => true }
        case None => Future(false)
      }
    } yield r
  }

  /**
   * Update unfollowers list of the user
   * @param users users
   * @param follower follower
   * @return a future indicates whether the update is successful
   */
  private def updateUnfollower(users: List[String], follower: String): Future[Boolean] = {
    for {
      user <- RepositoryFactory.factory.userRepository.getUser(UUID.fromString(follower))
      r <- user match {
        case Some(u) => RepositoryFactory.factory.userRepository.updateUser(User(id = u.id,
          groupMembers = u.groupMembers,
          realName = u.realName,
          nickName = u.nickName,
          email = u.email,
          password = u.password,
          avatar = u.avatar,
          description = u.description,
          followers = u.followers,
          followUsers = (u.followUsers -- users).toList.distinct.toSet,
          registeredEvents = u.registeredEvents,
          applyingEvents = u.applyingEvents,
          updatedTime = DateTime.now())).map { _ => true }
        case None => Future(false)
      }
    } yield r
  }

  /**
   * Validte the event register info
   * @param eventRegisterInfo the event register info
   */
  private def validate(eventRegisterInfo: EventRegisterInfo) = {
    if (eventRegisterInfo.registerUserIds.length <= 1) {
      throw new Exception(" members of a group at least are two. ")
    }
  }

  /**
   * Get event details with context
   * @param fullEvent the full event pair
   * @return the event details with context
   */
  private def getDetails(fullEvent: (Event, EventExtension)): Future[JsValue] = {
    val (e, ext) = fullEvent
    for {
      comments <- RepositoryFactory.factory.commentRepository.getCommentByTargetId(e.id)
      uids = (comments.map(_.userId) ++
        ext.applyingUsers.keySet.toSeq.map(UUID.fromString)).distinct
      usersOpt <- Future.sequence(uids.map(RepositoryFactory.factory.userRepository.getUser))
      users = usersOpt.filter(_.isDefined).map(_.get)
    } yield ModelConverter.toEventDetails(fullEvent, comments, users.toSet)
  }

  /**
   * Get full event by id
   * @param id event id
   * @return thef ull event
   */
  private def getFullEvent(id: UUID): Future[Option[(Event, EventExtension)]] = {
    RepositoryFactory.factory.eventRepository.getEvent(id).flatMap {
      case Some(e) => getEventExt(e)
      case _ => Future(None)
    }
  }

  /**
   * Get event ext
   * @param e event
   * @return event ext
   */
  private def getEventExt(e: Event): Future[Option[(Event, EventExtension)]] = {
    RepositoryFactory.factory.eventExtRepository.getEventExtension(e.id).map {
      case Some(ext) => Some((e, ext))
      case _ => None
    }
  }

  /**
   * Combine event sequence that has equal id
   * @param events events
   * @param exts event exts
   * @return  combined events
   */
  private def combineEventSeqs(events: Seq[Event], exts: Seq[EventExtension]): Seq[(Event, EventExtension)] = {
    def findExt(id: UUID): EventExtension = exts.find(_.id == id).get
    val es = events.filter(e => exts.exists(_.id == e.id))
    es.map(e => (e, findExt(e.id)))
  }

  /**
   * Apply policy to the event
   * @param seats the maximum number of seats
   * @param eventId the event id
   * @return a future indicates whether the applying was successful
   */
  private def applyPolicyToEvent(seats: Int, eventId: UUID): Future[Boolean] = {
    for {
      eventExt <- RepositoryFactory.factory.eventExtRepository.getEventExtension(eventId)
      event <- RepositoryFactory.factory.eventRepository.getEvent(eventId)
      r <- eventExt match {
        case Some(e) if e.registeredUsers.size < seats => {

          val selectedUsers = ApplyRegistrationPolicy.applyPolicy(seats, e.registerPolicy, e.applyingUsers)

          val updatingEventExt = EventExtension(
            id = e.id,
            presenter = e.presenter,
            presenterLogo = e.presenterLogo,
            presenterEmail = e.presenterEmail,
            presenterTitle = e.presenterTitle,
            prerequisites = e.prerequisites,
            tags = e.tags,
            attachments = e.attachments,
            registerPolicy = e.registerPolicy,
            likeUsers = e.likeUsers,
            registeredUsers = selectedUsers,
            applyingUsers = e.applyingUsers,
            followers = e.followers)

          val res = RepositoryFactory.factory.eventExtRepository.updateEventExtension(updatingEventExt).map { _ =>
            true
          }

          NotificationService.userSelectedPush(event.get.title, event.get.id.toString, event.get.category, selectedUsers.keySet)

          res
        }
        case None => Future {
          false
        }
      }
    } yield r
  }
}
