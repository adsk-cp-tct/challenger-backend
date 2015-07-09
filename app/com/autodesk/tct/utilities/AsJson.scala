package com.autodesk.tct.utilities

import java.util.{UUID, Date}

import org.joda.time.DateTime
import play.api.libs.json.Json._
import play.api.libs.json._

import scala.language.postfixOps

/**
 * Json helper
 *
 * Converts a Map or String to Json
 */
object AsJson {

  /**
   * Converts a Map to Json
   *
   * @param data the data map
   * @return the JsValue
   */
  def apply(data: Map[String, Any]): JsValue = {

    val wrapped: Map[String, JsValue] = data flatMap {

      case (key, value: String) => Some(key, JsString(value))

      case (key, value: Int) => Some(key, JsNumber(value))

      case (key, value: Long) => Some(key, JsNumber(value))

      case (key, value: Float) => Some(key, JsNumber(BigDecimal(value)))

      case (key, value: Double) => Some(key, JsNumber(value))

      case (key, value: Boolean) => Some(key, JsBoolean(value))

      case (key, value: Date) => Some(key, JsString(value.toString))

      case (key, value: DateTime) => Some(key, JsString(value.toString))

      case (key, value: UUID) => Some(key, JsString(value.toString))

      case (key, value: JsValue) => Some(key, value)

      case (key, value: Seq[_]) => Some(key, JsArray(value.map(v => JsString(v.toString))))

      case (key, value: Set[_]) => Some(key, JsArray(value.toSeq.map(v => JsString(v.toString))))

      case (key, value: Map[_, _]) =>
        Some(key, AsJson(value map (
          e => (e._1.toString, e._2))
        ))

      case (key, value) => Some(key.toString, JsString(value.toString))
    }

    toJson(wrapped toMap)
  }

  /**
   * Converts a String to Json
   *
   * @param data the JSON string to convert
   * @return a JsValue
   */
  def apply(data: String): JsValue = try {
    parse(data)
  } catch {
    case _: Throwable => toJson(Map[String, String]())
  }
}
