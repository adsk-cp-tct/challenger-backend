package com.autodesk.tct.services

import com.autodesk.tct.challenger.data.repositories.{Comment, RepositoryFactory}
import com.autodesk.tct.models.tools.JsonConverter
import play.api.libs.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object CommentService {

  /**
   * Create a comment from a given formatted json
   *
   * @param obj input json
   * @return the future of the possible created comment
   */
  def createComment(obj: JsValue): Future[Map[String, String]] = {

    val comment = JsonConverter.toComment(obj)

    val insertF = RepositoryFactory.factory.commentRepository.insertComment(comment)
    insertF.map {
      case true => Map(
        "id" -> comment.id.toString,
        "content" -> comment.content,
        "createdTime" -> comment.createdTime.toString()
      )
      case false => Map()
    }
  }

  /**
   * Remove a comment
   *
   * @param comment the comment id
   * @return the future of the possible success (true or false)
  */
  def deleteComment(comment: Comment): Future[Boolean] = RepositoryFactory.factory.commentRepository.deleteComment(comment)

}
