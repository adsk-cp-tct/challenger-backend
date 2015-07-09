/*
 *  Copyright (c) 2015 by PROJECT Challenger
 *  All rights reserved.
 */
package com.autodesk.tct.challenger.common

/**
 * Defines common constants/key word/os specific values
 */
object CommonConstants {

  /**
   * Database value separator - used to separator values in one field
   */
  object Database {
    val DB_SEPARATOR = '|'
  }

  /**
   * Activity verbs - the verbs used to format a human readable sentence
   *  of this activity
   */
  object ActivityVerb {

    val LIKE = "like"
    val UNLIKE = "unlike"

    val FOLLOW = "follow"
    val UNFOLLOW = "unfollow"

    val REGISTER = "register"
    val UNREGISTER = "unregister"

    val COMMENT = "comment"

    val CREATE = "create"
  }

  /**
   * Types of activity
   */
  object ActivityObjType {
    val EVENT = "event"
    val USER = "user"
    val IDEA = "idea"
  }

}
