package com.autodesk.tct.controllers

import java.io.File

import com.autodesk.tct.services.ImageService
import play.Play
import play.api.mvc.{Action, AnyContent, Controller, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ImageController extends Controller {

  private val uploadPath = Play.application().configuration().getString("application.image.uploadPath")
  private val rootPath = Play.application().configuration().getString("application.image.rootPath")

  /*
  UUID + fileType.
  Sample is: 284066c7-02b9-49e2-b35f-1a5d61ee90a2.png
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

  def uploadImage = Action.async(parse.multipartFormData) {
    request => {
      request.body.file("picture") match {
        case Some(picture) =>
          val imageFolder = new File(uploadPath)
          imageFolder.mkdirs() //if exist, just return false

          val newFileName = generateFileName(picture.filename)
          picture.ref.moveTo(new File(uploadPath + newFileName))

          Future(Ok(newFileName))

        case None => Future(BadRequest("Upload picture not available"))
      }
    }
  }

  /*
  First look up image folder to see whether the required thumbnail(filename&size) exists. size is: width x height
  If it exists, return thumbnail name directly. Else generate required thumbnail and return new thumbnail name.
   */
  def getThumbnail(fileName: String, size: String) = Action.async(parse.tolerantText) {
    request => {
      try {
        val thumbnailName = ImageService.getThumbnail(fileName, size)
        Future(Results.Ok(thumbnailName))
      } catch {
        case e: Throwable => Future(InternalServerError("Get thumbnail failed" + e.getMessage))
      }
    }
  }

  private val AbsolutePath = """^(/|[a-zA-Z]:\\).*""".r

  def at(file: String): Action[AnyContent] = Action {
    request => getImageStream(file)
  }

  def getThumbnailStream(fileName: String, size: String) = Action.async(parse.tolerantText) {
    request => {
      try {
        Future {
          getImageStream(ImageService.getThumbnail(fileName, size))
        }
      } catch {
        case e: Throwable => Future(InternalServerError("Get thumbnail failed" + e.getMessage))
      }
    }
  }

  private def getImageStream(fileName: String) = {
    val fileToServe = rootPath match {
      case AbsolutePath(_) =>
        val newFile = new File(rootPath, fileName)

        newFile
      case _ => new File(Play.application.getFile(rootPath), fileName)
    }

    if (fileToServe.exists) {
      Ok.sendFile(fileToServe, inline = true)
    } else {
      NotFound
    }
  }
}
