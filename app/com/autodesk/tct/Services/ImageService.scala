package com.autodesk.tct.services

import java.io.File

import com.autodesk.tct.utilities.DistProvider
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.ScaleMethod.FastScale
import play.Play

/**
 * Handles image upload/download
 */
object ImageService {
  private val uploadPath = Play.application().configuration().getString("application.image.uploadPath")

  /**
   * Generates a new file name as "UUID + fileType".
   *  Sample is: 284066c7-02b9-49e2-b35f-1a5d61ee90a2.png
   * @param fileName the original input file name
   * @return the new file name
   */
  private def generateFileName(fileName: String) = {
    val filePrefix = java.util.UUID.randomUUID.toString
    val fileSplit = fileName.split('.') toList
    val fileType = fileSplit.size match {
      case 2 => fileSplit(1)
      case _ => ""
    }

    filePrefix + "." + fileType
  }

  /**
   * Generates thumbnail file name as: originalFileUUID + "-thumbnail-" + size + ".png"
   * Sample is: 284066c7-02b9-49e2-b35f-1a5d61ee90a2-thumbnail-300x200.png
   * @param fileName the original file name
   * @param size the size of the thumbnail
   * @return the new thumbnail file name
   */
  private def generateThumbnailName(fileName: String, size: String) = {
    val fileSplit = fileName.split('.') toList
    val fileUUID = fileSplit.size match {
      case 2 => fileSplit(0)
      case _ => ""
    }

    fileUUID + "-thumbnail-" + size + ".png"
  }

  /**
   * Get thumbnail
   * @param fileName the thumbnail file name
   * @param size the size of the thumbnail
   * @return the thumbnail path
   */
  def getThumbnail(fileName: String, size: String): String = {
    val thumbnailName = generateThumbnailName(fileName, size)
    val thumbnailPath = uploadPath + thumbnailName

    new File(thumbnailPath).exists match {
      case true =>
        thumbnailName
      case false =>
        try {
          val (in, length) = DistProvider.read(uploadPath + fileName) // input stream
          val (width, height) = size.split("x") match {
              case Array(w, h, _*) => (w.toInt, h.toInt)
              case _ => (90, 90)
            }
          val thumbnail = Image(in).scaleTo(width, height, FastScale)
          thumbnail.write(new File(thumbnailPath))

          thumbnailName
        } catch {
          case e: Throwable => throw e
        }
    }
  }

}
