package com.autodesk.tct.utilities

import java.io._

/**
 * Helper object that reads dist file
 */
object DistProvider {
  /**
   * Reads the dist file. Note: this function does not automatically close the InputStream
   *
   * @param pathFileName the dist file path
   * @return the InputStream and its size.
   */
  def read(pathFileName: String): (InputStream, Long) = {
    val file = new File(pathFileName)
    val inputStream = new FileInputStream(file)
    (inputStream, file.length())
  }
}
