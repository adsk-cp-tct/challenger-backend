package com.autodesk.tct

import akka.actor.{ActorSystem, Props}
import com.autodesk.tct.challenger.cassandra.CassandraClient
import com.autodesk.tct.challenger.data.repositories.RepositoryFactory
import com.autodesk.tct.services.UserService
import com.autodesk.tct.share.{ApplyPolicy, ApplyRegistrationPolicy}
import play._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Global boot object
 */
class Boot extends GlobalSettings {

  /**
   * Defines the startup actions
   * @param app the play app
   */
  override def onStart(app: Application) {
    super.onStart(app)

    /**
     * Get configuration
     */
    val conf = Play.application.configuration

    /**
     * Initialize cassandra metadata
     */
    CassandraClient.init(
      conf.getStringList("application.db.cassandra.addresses").asScala.toList,
      conf.getString("application.db.cassandra.namespace")
    )

    /**
     * Load cassandra driver
     */
    RepositoryFactory.driver(conf.getString("application.db.repository_factory"))

    /**
     * Schedule to apply registration policy to all events which go beyond the due data
     */
    val system = ActorSystem("TctChallenger")
    val applyRegistrationPolicy = system.actorOf(Props(new ApplyRegistrationPolicy), name = "actor")
    system.scheduler.schedule(0.second, 6.hours, applyRegistrationPolicy, ApplyPolicy())

    /**
     * Create default admin account
     */
    UserService.createAdmin
  }

}
