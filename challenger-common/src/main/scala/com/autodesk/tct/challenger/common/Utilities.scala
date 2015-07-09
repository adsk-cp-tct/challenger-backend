/*
 *  Copyright (c) 2015 by PROJECT Challenger
 *  All rights reserved.
 */
package com.autodesk.tct.challenger.common

/**
 * Utilities ...
 */
object Utilities {

  /**
   * Connect a sequence of string to a single string
   * @param a a sequence of strings to connect
   * @param sep the separator, default to [[CommonConstants.Database.DB_SEPARATOR]]
   * @return the connected string
   */
  def a2s(a: Seq[String], sep: Char = CommonConstants.Database.DB_SEPARATOR): String = {
    a.mkString(sep.toString)
  }

  /**
   * Split a single string into a sequence of strings
   * @param s the original single string
   * @param sep the separator, default to [[CommonConstants.Database.DB_SEPARATOR]]
   * @return a sequence of strings
   */
  def s2a(s: String, sep: Char = CommonConstants.Database.DB_SEPARATOR): Seq[String] = {
    s.split(sep)
  }
}