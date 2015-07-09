package com.autodesk.tct.challenger.cassandra

import com.autodesk.tct.challenger.data.repositories._
import com.websudos.phantom.batch._

/**
 * Repository factory that generates different repositories to interact with different
 *  table objects
 */
class RepositoryFactory extends IRepositoryFactory {
  var _activityRepository: Option[IActivityRepository] = None
  var _eventExtRepository: Option[IEventExtensionRepository] = None
  var _commentRepository: Option[ICommentRepository] = None
  var _userRepository: Option[IUserRepository] = None
  var _eventRepository: Option[IEventRepository] = None
  var _ideaRepository: Option[IIdeaRepository] = None

  /**
   * Get the activity repository
   * @return the activity repository
   */
  override def activityRepository: IActivityRepository = {
    _activityRepository match {
      case Some(ar) => ar
      case None =>
        _activityRepository = Some(ActivityRepository)
        _activityRepository.get
    }
  }

  /**
   * Get the event extension repository
   * @return the event extension repository
   */
  override def eventExtRepository: IEventExtensionRepository = {
    _eventExtRepository match {
      case Some(er) => er
      case None =>
        _eventExtRepository = Some(EventExtensionRepository)
        _eventExtRepository.get
    }
  }

  /**
   * Get the comment repository
   * @return the comment repository
   */
  override def commentRepository: ICommentRepository = {
    _commentRepository match {
      case Some(cr) => cr
      case None =>
        _commentRepository = Some(CommentRepository)
        _commentRepository.get
    }
  }

  /**
   * Get the user extension repository
   * @return the user extension repository
   */
  override def userRepository: IUserRepository = {
    _userRepository match {
      case Some(ur) => ur
      case None =>
        _userRepository = Some(UserRepository)
        _userRepository.get
    }
  }

  /**
   * Get the event repository
   * @return the event repository
   */
  override def eventRepository: IEventRepository = {
    _eventRepository match {
      case Some(er) => er
      case None =>
        _eventRepository = Some(EventRepository)
        _eventRepository.get
    }
  }

  /**
   * Get the idea repository
   * @return the idea repository
   */
  override def ideaRepository: IIdeaRepository = {
    _ideaRepository match {
      case Some(er) => er
      case None =>
        _ideaRepository = Some(IdeaRepository)
        _ideaRepository.get
    }
  }
}
