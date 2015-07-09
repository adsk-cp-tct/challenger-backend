package com.autodesk.tct.services

import java.util.UUID

import com.autodesk.tct.challenger.data.repositories.{User, Idea, RepositoryFactory}
import com.autodesk.tct.models.tools._
import org.joda.time.DateTime
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Handles Idea related operations
 */
object IdeaService {

  /**
   * Create an idea
   * @param obj a JS object that contains at least: title,description,thumbnails,createdBy
   * @return a future contains the possible generated Idea object
   */
  def createIdea(obj: JsObject): Future[Option[JsValue]] = {

    val idea = JsonConverter.toIdea(obj)

    lazy val res = ModelConverter.toIdeaMetadata(idea)

    val insertF = RepositoryFactory.factory.ideaRepository.insertIdea(idea)
    insertF.map {
      case true => Some(res)
      case false => None
    }
  }

  /**
   * Get idea details itself
   * @param id idea id
   * @return the details of the idea
   */
  def getIdeaDetail(id: UUID): Future[Option[JsValue]] = {
    RepositoryFactory.factory.ideaRepository.getIdea(id).flatMap {
      case Some(i) => getInDetail(i)
      case _ => Future {
        None
      }
    }
  }

  /**
   * Get idea details with context
   * @param idea the idea
   * @return the idea details with all necessary context
   */
  private def getInDetail(idea: Idea): Future[Option[JsValue]] = {
    for {
      comments <- RepositoryFactory.factory.commentRepository.getCommentByTargetId(idea.id)
      uids = (comments.map(_.userId) ++ Seq(idea.createdBy) ++ idea.followers).distinct
      usersOpt <- Future.sequence(uids.map(RepositoryFactory.factory.userRepository.getUser))
      users = usersOpt.filter(_.isDefined).map(_.get)
    } yield Some(ModelConverter.toIdeaDetails(idea, comments, users.toSet))

  }

  /**
   * Join ideas and user info together
   * @param ideas idea list
   * @param users user list
   * @return idea list that contain user information
   */
  private def joinIdeaList(ideas: Seq[Idea], users: Seq[User]): Seq[JsValue] = {
    val umap = users.map(u => u.id -> u).toMap
    ideas.map(i => {
      val user = umap.get(i.createdBy)
      ModelConverter.toIdeaMetadata(i).as[JsObject] ++
      JsObject(Seq(
        "createdUser" -> JsString(user.map(_.nickName).getOrElse("")),
        "createdAvatar" -> JsString(user.map(_.avatar).getOrElse(""))
      ))
    })
  }

  /**
   * Get idea list maximum by limit
   * @param limit the limit number
   * @return a future of possible idea list
   */
  def getIdeaList(limit: Int): Future[JsValue] = {
    for {
      ideas <- RepositoryFactory.factory.ideaRepository.getAll(limit)
      uids = ideas.map(_.createdBy)
      usersOpt <- Future.sequence(uids.map(RepositoryFactory.factory.userRepository.getUser))
      users = usersOpt.filter(_.isDefined).map(_.get)
    } yield ModelConverter.toIdeaJsList(joinIdeaList(ideas, users))
  }

  /**
   * Get idea list maximum by limit. between start/end date
   * @param limit the limit number
   * @param start the start date
   * @param end the end date
   * @return a future of possible idea list
   */
  def getIdeaList(limit: Int, start: DateTime, end: DateTime): Future[JsValue] = {
    for {
      ideas <- RepositoryFactory.factory.ideaRepository.getAll(limit, start, end)
      uids = ideas.map(_.createdBy)
      usersOpt <- Future.sequence(uids.map(RepositoryFactory.factory.userRepository.getUser))
      users = usersOpt.filter(_.isDefined).map(_.get)
    } yield ModelConverter.toIdeaJsList(joinIdeaList(ideas, users))
  }

  /**
   * Get idea list maximum by limit from specific user
   * @param uid the user id
   * @param limit the limit number
   * @return a future of possible idea list
   */
  def getIdeaListByUser(uid: UUID, limit: Int): Future[JsValue] = {
    for {
      ideas <- RepositoryFactory.factory.ideaRepository.getByUid(uid, limit)
      uids = ideas.map(_.createdBy)
      usersOpt <- Future.sequence(uids.map(RepositoryFactory.factory.userRepository.getUser))
      users = usersOpt.filter(_.isDefined).map(_.get)
    } yield ModelConverter.toIdeaJsList(joinIdeaList(ideas, users))
  }

  /**
   * Get idea list maximum by limit from specific user
   * @param uid the user id
   * @param limit the limit number
   * @param start the start date
   * @param end the end date
   * @return a future of possible idea list
   */
  def getIdeaListByUser(uid: UUID, limit: Int, start: DateTime, end: DateTime): Future[JsValue] = {
    val future = RepositoryFactory.factory.ideaRepository.getByUid(uid, limit, start, end)
    future.map(ModelConverter.toIdeaList)
  }

  /**
   * Like an idea by specific user
   * @param ideaId idea id
   * @param userId user id
   * @return a future of highly customized key value store
   */
  def likeIdeaByUser(ideaId: String, userId: UUID): Future[Map[String, String]] = {
    likeIdea(ideaId, List(userId))
  }

  /**
   * Unlike an idea by specific user
   * @param ideaId idea id
   * @param userId user id
   * @return a future of highly customized key value store
   */
  def unlikeIdeaByUser(ideaId: String, userId: UUID): Future[Map[String, String]] = {
    unlikeIdea(ideaId, List(userId))
  }

  /**
   * Follow an idea by specific user
   * @param ideaId idea id
   * @param userId user id
   * @return a future of highly customized key value store
   */
  def followIdeaByUser(ideaId: String, userId: UUID): Future[Map[String, String]] = {
    followIdea(ideaId, List(userId))
  }

  /**
   * Unfollow an idea by specific user
   * @param ideaId idea id
   * @param userId user id
   * @return a future of highly customized key value store
   */
  def unfollowIdeaByUser(ideaId: String, userId: UUID): Future[Map[String, String]] = {
    unfollowIdea(ideaId, List(userId))
  }

  /**
   * Like an idea by a set of users
   * @param ideaId idea id
   * @param userIds user ids
   * @return a future of highly customized key value store
   */
  def likeIdea(ideaId: String, userIds: List[UUID]): Future[Map[String, String]] = {
    for {
      idea <- RepositoryFactory.factory.ideaRepository.getIdea(UUID.fromString(ideaId))
      r <- idea match {
        case Some(e@Idea(id, title, description, thumbnails, followers, likedUsers, createdDate, createdBy, _)) => {
          val updatingIdea = Idea(id,
            title,
            description,
            thumbnails,
            followers,
            (likedUsers ++ userIds).toList.distinct.toSet,
            createdDate,
            createdBy)
          RepositoryFactory.factory.ideaRepository.updateIdea(updatingIdea).map { _ =>
            Map("status" -> "success")
          }
        }
        case None => Future(Map("status" -> "fails", "message" -> "current idea is not existed"))
      }
    } yield r
  }

  /**
   * Unlike an idea by a set of users
   * @param ideaId idea id
   * @param userIds user ids
   * @return a future of highly customized key value store
   */
  def unlikeIdea(ideaId: String, userIds: List[UUID]): Future[Map[String, String]] = {
    for {
      idea <- RepositoryFactory.factory.ideaRepository.getIdea(UUID.fromString(ideaId))
      r <- idea match {
        case Some(e@Idea(id, title, description, thumbnails, followers, likedUsers, createdDate, createdBy, _)) => {
          val updatingIdea = Idea(id,
            title,
            description,
            thumbnails,
            followers,
            (likedUsers -- userIds).toList.distinct.toSet,
            createdDate,
            createdBy)
          RepositoryFactory.factory.ideaRepository.updateIdea(updatingIdea).map { _ =>
            Map("status" -> "success")
          }
        }
        case None => Future(Map("status" -> "fails", "message" -> "current idea is not existed"))
      }
    } yield r
  }

  /**
   * Follow an idea by a set of users
   * @param ideaId idea id
   * @param userIds user ids
   * @return a future of highly customized key value store
   */
  def followIdea(ideaId: String, userIds: List[UUID]): Future[Map[String, String]] = {
    for {
      idea <- RepositoryFactory.factory.ideaRepository.getIdea(UUID.fromString(ideaId))
      r <- idea match {
        case Some(e@Idea(id, title, description, thumbnails, followers, likedUsers, createdDate, createdBy, _)) => {
          val updatingIdea = Idea(id,
            title,
            description,
            thumbnails,
            (followers ++ userIds).toList.distinct.toSet,
            likedUsers,
            createdDate,
            createdBy)
          RepositoryFactory.factory.ideaRepository.updateIdea(updatingIdea).map { _ =>
            Map("status" -> "success")
          }
        }
        case None => Future(Map("status" -> "fails", "message" -> "current idea is not existed"))
      }
    } yield r
  }

  /**
   * Unfollow an idea by a set of users
   * @param ideaId idea id
   * @param userIds user ids
   * @return a future of highly customized key value store
   */
  def unfollowIdea(ideaId: String, userIds: List[UUID]): Future[Map[String, String]] = {
    for {
      idea <- RepositoryFactory.factory.ideaRepository.getIdea(UUID.fromString(ideaId))
      r <- idea match {
        case Some(e@Idea(id, title, description, thumbnails, followers, likedUsers, createdDate, createdBy, _)) => {
          val updatingIdea = Idea(id,
            title,
            description,
            thumbnails,
            (followers -- userIds).toList.distinct.toSet,
            likedUsers,
            createdDate,
            createdBy)
          RepositoryFactory.factory.ideaRepository.updateIdea(updatingIdea).map { _ =>
            Map("status" -> "success")
          }
        }
        case None => Future(Map("status" -> "fails", "message" -> "current idea is not existed"))
      }
    } yield r
  }
}
