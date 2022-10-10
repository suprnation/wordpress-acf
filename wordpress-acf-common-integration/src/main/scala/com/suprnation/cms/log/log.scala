package com.suprnation.cms

import com.suprnation.cms.executors.FieldExecutionPlan
import com.suprnation.cms.log.Colours._
import com.suprnation.cms.marker.CmsPostIdentifier
import com.suprnation.cms.result.{Result, SearchInDatabase}
import com.suprnation.cms.tokens.{CmsFieldToken, CmsToken, ParameterisedRelationshipToken, PostToken}
import com.suprnation.cms.types.PostId

import scala.collection.JavaConverters._

package object log {

  sealed trait ExecutionLogger {
    def logCaching: Boolean

    def logInstantiation: Boolean

    def logNewExecution: Boolean

  }

  sealed trait CacheMetric

  trait CmsLogging {
    def logColour: LogColour
  }

  trait ResolverLogging extends ExecutionLogging {
    override val logColour: String = Colours.CYAN
  }

  trait ExecutorLogging extends ExecutionLogging {
    override val logColour: String = Colours.BLUE
  }

  abstract class ExecutionLogging extends CmsLogging {
    def logExecutionRelationships(depth: Int, relationshipTokens: List[ParameterisedRelationshipToken[_]], cacheMetric: CacheMetric, ids: Iterable[PostId])(implicit executionLogger: ExecutionLogger): Unit =
      logExecutionInternal(depth, relationshipTokens.foldLeft("")((acc, token) => s"$acc $token") + " " + foldIds(ids), cacheMetric)

    def logExecutionPostToken(depth: Int, postToken: PostToken[_], cacheMetric: CacheMetric)(implicit executionLogger: ExecutionLogger): Unit =
      logExecutionInternal(depth, postToken.fields.foldLeft(postToken.toString)((acc, field) => s"$acc $field "), cacheMetric)

    def logExecutionFieldTokens(depth: Int, fieldTokens: List[CmsFieldToken], cacheMetric: CacheMetric, ids: Iterable[PostId])(implicit executionLogger: ExecutionLogger): Unit =
      logExecutionInternal(depth, fieldTokens.foldLeft("")((acc, field) => s"$acc$field  ") + foldIds(ids), cacheMetric)

    def logExecution(depth: Int, cmsToken: CmsToken, cacheMetric: CacheMetric, multipart: Boolean = false)(implicit executionLogger: ExecutionLogger): Unit =
      logExecutionInternal(depth, cmsToken.toString, cacheMetric, multipart)

    def logExecutionWithPostId(depth: Int, cmsToken: CmsToken, postId: PostId, cacheMetric: CacheMetric)(implicit executionLogger: ExecutionLogger): Unit =
      logExecutionInternal(depth, s"$cmsToken [ $postId ]", cacheMetric)

    def logExecutionOnBehalf(fieldExecutionPlan: FieldExecutionPlan[CmsFieldToken, _], depth: Int, cmsToken: CmsToken, cacheMetric: CacheMetric)(implicit executionLogger: ExecutionLogger): Unit =
      fieldExecutionPlan.logExecution(depth, cmsToken, cacheMetric)

    def logExecution(depth: Int, cmsToken: CmsToken, cacheMetric: CacheMetric)(implicit executionLogger: ExecutionLogger): Unit =
      logExecutionInternal(depth, s"$cmsToken", cacheMetric)

    private def logExecutionInternal(depth: Int, msg: String, cacheMetric: CacheMetric, multipart: Boolean = false)(implicit executionLogger: ExecutionLogger): Unit = {
      if (executionLogger.logCaching) {
        val multipartPrefix = if (multipart) "<--" else ""
        println(s"${getSpaces(depth)}  $logColour$multipartPrefix${getClass.getSimpleName}$cacheMetric $YELLOW$msg$RESET")
        if (multipart) println
      }
    }

    def logExecutionWithPostIds(depth: Int, cmsToken: CmsToken, cacheMetric: CacheMetric, ids: Iterable[PostId])(implicit executionLogger: ExecutionLogger): Unit =
      logExecutionInternal(depth, s"$cmsToken ${foldIds(ids)}", cacheMetric)

    def logInstantiation(depth: Int, postToken: PostToken[_], id: PostId)(implicit executionLogger: ExecutionLogger): Unit = {
      if (executionLogger.logCaching) {
        val spaces = getSpaces(depth)
        println(s"$spaces  ${PURPLE}Instantiating ${postToken.postType}:${postToken.source.getSimpleName} [ $id ]$RESET")
      }
    }

    def logNewExecution(depth: Int, postToken: PostToken[_], ids: Iterable[PostId])(implicit executionLogger: ExecutionLogger): Unit = {
      if (executionLogger.logCaching) {
        println(s"\n${getSpaces(depth)}  $BLUE--> New Execution $postToken ${foldIds(ids)}$RESET")
      }
    }

    def getSpaces(depth: Int): LogColour = Range(1, depth).foldLeft("")((acc, _) => s"$acc\t")

    private def foldIds(ids: Iterable[PostId]) = "[ " + (if (ids.isEmpty) "Full Scan " else ids.toSet.foldLeft("")((acc, id) => s"$acc$id ")) + "]"

  }

  case class MultipleResultCacheMetric(foundInCache: Int, notFoundInCache: Int) extends CacheMetric {
    override def toString: String = {
      val found = if (foundInCache > 0) s"$GREEN[Hit:$foundInCache]" else ""
      val miss = if (notFoundInCache > 0) s"$RED[Miss:$notFoundInCache]" else ""
      found + miss
    }
  }

  case class FoundInCache(result: Result[_]) extends CacheMetric {
    override val toString: String = {
      val stringValue =
        if (result.isDefined) {
          val value = result.get
          value match {
            case javaList: java.util.List[_] =>
              val list = javaList.asScala
              if (list.headOption.exists(_.isInstanceOf[CmsPostIdentifier])) {
                list.map(_.asInstanceOf[CmsPostIdentifier]).foldLeft(" ")((acc, value) => s"$acc${value.getWordpressId} ")
              } else {
                result.toString
              }
            case _ =>
              result.toString
          }

        } else {
          result.toString
        }
      s"$GREEN[Hit:1] [Value:$stringValue]"
    }
  }

  object ExecutionLogger {

    object IgnoreAllExecutionLogger extends ExecutionLogger {
      override def logCaching: Boolean = false

      override def logInstantiation: Boolean = false

      override def logNewExecution: Boolean = false
    }

    object PrintAllExecutionLogger extends ExecutionLogger {
      override def logCaching: Boolean = true

      override def logInstantiation: Boolean = true

      override def logNewExecution: Boolean = true
    }

  }

  object CacheMetric {
    def apply(result: Result[_]): CacheMetric = if (result == SearchInDatabase) NotFoundInCache else FoundInCache(result)
  }

  object Intermediary extends CacheMetric {
    override def toString: LogColour = s"${GREEN}Intermediary"
  }

  object NotFoundInCache extends CacheMetric {
    override val toString: String = s"$RED[Miss:1]"
  }

  object Colours {
    type LogColour = String

    // Reset
    val RESET = "\u001b[0m" // Text Reset

    // Regular Colors
    val BLACK = "\u001b[030m" // BLACK
    val RED = "\u001b[031m" // RED
    val GREEN = "\u001b[032m" // GREEN
    val YELLOW = "\u001b[033m" // YELLOW
    val BLUE = "\u001b[034m" // BLUE
    val PURPLE = "\u001b[035m" // PURPLE
    val CYAN = "\u001b[036m" // CYAN
    val WHITE = "\u001b[037m" // WHITE

    // Bold
    val BLACK_BOLD = "\u001b[130m" // BLACK
    val RED_BOLD = "\u001b[131m" // RED
    val GREEN_BOLD = "\u001b[132m" // GREEN
    val YELLOW_BOLD = "\u001b[133m" // YELLOW
    val BLUE_BOLD = "\u001b[134m" // BLUE
    val PURPLE_BOLD = "\u001b[135m" // PURPLE
    val CYAN_BOLD = "\u001b[136m" // CYAN
    val WHITE_BOLD = "\u001b[137m" // WHITE

    // Underline
    val BLACK_UNDERLINED = "\u001b[430m" // BLACK
    val RED_UNDERLINED = "\u001b[431m" // RED
    val GREEN_UNDERLINED = "\u001b[432m" // GREEN
    val YELLOW_UNDERLINED = "\u001b[433m" // YELLOW
    val BLUE_UNDERLINED = "\u001b[434m" // BLUE
    val PURPLE_UNDERLINED = "\u001b[435m" // PURPLE
    val CYAN_UNDERLINED = "\u001b[436m" // CYAN
    val WHITE_UNDERLINED = "\u001b[437m" // WHITE

    // Background
    val BLACK_BACKGROUND = "\u001b[40m" // BLACK
    val RED_BACKGROUND = "\u001b[41m" // RED
    val GREEN_BACKGROUND = "\u001b[42m" // GREEN
    val YELLOW_BACKGROUND = "\u001b[43m" // YELLOW
    val BLUE_BACKGROUND = "\u001b[44m" // BLUE
    val PURPLE_BACKGROUND = "\u001b[45m" // PURPLE
    val CYAN_BACKGROUND = "\u001b[46m" // CYAN
    val WHITE_BACKGROUND = "\u001b[47m" // WHITE

    // High Intensity
    val BLACK_BRIGHT = "\u001b[090m" // BLACK
    val RED_BRIGHT = "\u001b[091m" // RED
    val GREEN_BRIGHT = "\u001b[092m" // GREEN
    val YELLOW_BRIGHT = "\u001b[093m" // YELLOW
    val BLUE_BRIGHT = "\u001b[094m" // BLUE
    val PURPLE_BRIGHT = "\u001b[095m" // PURPLE
    val CYAN_BRIGHT = "\u001b[096m" // CYAN
    val WHITE_BRIGHT = "\u001b[097m" // WHITE

    // Bold High Intensity
    val BLACK_BOLD_BRIGHT = "\u001b[190m" // BLACK
    val RED_BOLD_BRIGHT = "\u001b[191m" // RED
    val GREEN_BOLD_BRIGHT = "\u001b[192m" // GREEN
    val YELLOW_BOLD_BRIGHT = "\u001b[193m"
    // YELLOW
    val BLUE_BOLD_BRIGHT = "\u001b[194m" // BLUE
    val PURPLE_BOLD_BRIGHT = "\u001b[195m"
    // PURPLE
    val CYAN_BOLD_BRIGHT = "\u001b[196m" // CYAN
    val WHITE_BOLD_BRIGHT = "\u001b[197m" // WHITE

    // High Intensity backgrounds
    val BLACK_BACKGROUND_BRIGHT = "\u001b[0100m"
    // BLACK
    val RED_BACKGROUND_BRIGHT = "\u001b[0101m"
    // RED
    val GREEN_BACKGROUND_BRIGHT = "\u001b[0102m"
    // GREEN
    val YELLOW_BACKGROUND_BRIGHT = "\u001b[0103m"
    // YELLOW
    val BLUE_BACKGROUND_BRIGHT = "\u001b[0104m"
    // BLUE
    val PURPLE_BACKGROUND_BRIGHT = "\u001b[0105m" // PURPLE
    val CYAN_BACKGROUND_BRIGHT = "\u001b[0106m" // CYAN
    val WHITE_BACKGROUND_BRIGHT = "\u001b[0107m" // WHITE
  }

}
