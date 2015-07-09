package com.autodesk.tct.share

import java.security.MessageDigest

/**
 * Utilities ...
 */
object Utils {

  /**
   * GET MD5 digest checksum from a input string
   * @param message the input string
   * @return the MD5 checksum
   */
  def md5(message: String): String = {
    new String(MessageDigest.getInstance("MD5").digest(message.getBytes))
  }
}
