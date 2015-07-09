/*
 *  Copyright (c) 2015 by PROJECT Challenger
 *  All rights reserved.
 */
package com.autodesk.tct.challenger.data.repositories

/**
 * Factory Interface - generates different repositories
 */
trait IRepositoryFactory {

  /**
   * Generate a repo for activity interacting
   * @return the activity repository
   */
  def activityRepository: IActivityRepository

  /**
   * Generate a repo for comment interacting
   * @return the comment repository
   */
  def commentRepository: ICommentRepository

  /**
   * Generate a repo for event interacting
   * @return the event repository
   */
  def eventRepository: IEventRepository

  /**
   * Generate a repo for event extension interacting
   * @return the event extension repository
   */
  def eventExtRepository: IEventExtensionRepository

  /**
   * Generate a repo for user interacting
   * @return the user extension repository
   */
  def userRepository: IUserRepository

  /**
   * Generate a repo for idea interacting
   * @return the idea repository
   */
  def ideaRepository: IIdeaRepository

}

/**
 * Factory implementation - generates real repositories
 */
object RepositoryFactory {
  var driverClassName: String = ""
  var factoryInstance: Option[IRepositoryFactory] = None

  /**
   * Set the driver
   * @param driverClass the full qualified class name
   */
  def driver(driverClass: String): Unit = {
    driverClassName = driverClass
  }

  /**
   * Get the actual factory instance
   * @return the evaluated factory
   */
  def factory: IRepositoryFactory = factoryInstance match {
    case Some(instance) => instance
    case None =>
      val newInstance = Class.forName(driverClassName).newInstance().asInstanceOf[IRepositoryFactory]
      factoryInstance = Some(newInstance)
      newInstance
  }
}
