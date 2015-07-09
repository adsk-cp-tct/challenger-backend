package com.autodesk.tct.challenger.cassandra

import com.datastax.driver.core.{Session, Cluster}

/**
 * Cassandra R/W communicate object
 */
object CassandraClient {
  /**
   * Cassandra connection string
   */
  private[this] var cassandraContactPoints: List[String] = List.empty

  /**
   * Cassandra namespace
   */
  private[this] var cassandraNameSpace: String = ""

  /**
   * Cassandra session
   */
  private[this] var _session: Option[Session] = None

  /**
   * Initialize cassandra connection metadata
   * @param contactPoints the cassandra connection string
   * @param nameSpace the cassandra namespace
   */
  def init(contactPoints: List[String], nameSpace: String): Unit = {
    (contactPoints.isEmpty, nameSpace.isEmpty) match {
      case (false, false) =>
        cassandraContactPoints = contactPoints
        cassandraNameSpace = nameSpace
      case _ => throw new Exception("CassandraClient initial fails, please give contactPoints and namespace of cassandra server")
    }
  }

  /**
   * Get the cassandra session, if not exist, create it
   * @return the cassandra session
   */
  def session: Session = {
    _session match {
      case Some(s) => s
      case None =>
        val cluster = Cluster.builder().addContactPoints(cassandraContactPoints: _*).build()
        _session = Some(cluster.connect(cassandraNameSpace))
        _session.get
    }
  }
}
