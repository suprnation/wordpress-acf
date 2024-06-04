package com.suprnation.cms.converter

import java.lang
import java.lang.annotation.Annotation
import com.suprnation.cms.annotations.{ForceUtc, Formatted, NumberCsv}
import com.suprnation.cms.enums.Formatting.{HtmlToTags, NewLineToBR, NoFormatting}
import com.suprnation.cms.utils.CmsReflectionUtils
import org.springframework.util.StringUtils

import java.time.{DateTimeException, LocalDate, ZoneId, ZoneOffset, ZonedDateTime}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

object PrimitiveTypeConverter {

  // Parsing the following formats: '20240322' '2024-03-22' '2024-03-22 15:52' '2024-03-22 15:52:34'  '2024-03-22T15:52:34' '2024-03-22T15:52:34Z' '2024-03-22T15:52:34.123' '2024-03-22T15:52:34.123Z' '2024-03-22T15:52:34.123456' '2024-03-22T15:52:34.123456Z'
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("[yyyyMMdd][yyyy-MM-dd[ HH:mm[:ss]]['T'HH:mm:ss[.SSSSSS][.SSS][z]]]").withZone(ZoneId.from(ZoneOffset.UTC))

  private def converters: Map[(Class[_], List[Class[_]]), PrimitiveTypeConverter] = Map(
    (classOf[String], List.empty) -> StringConverter(),
    (classOf[String], List(classOf[Formatted])) -> FormattedConverter(),
    (classOf[String], List(classOf[NumberCsv])) -> NumberCsvConverter(),
    (classOf[lang.Integer], List.empty) -> IntegerConverter(),
    (classOf[lang.Long], List.empty) -> LongConverter(),
    (classOf[lang.Double], List.empty) -> DoubleConverter(),
    (classOf[lang.Boolean], List.empty) -> BooleanConverter(),
    (classOf[ZonedDateTime], List(classOf[ForceUtc])) -> DateTimeConverter(true),
    (classOf[ZonedDateTime], List.empty) -> DateTimeConverter(false))

  def convert(value: String, targetClass: Class[_], annotations: List[_ >: Annotation]): Object = {
    val converter = if (targetClass.isEnum) Option(EnumConverter()) else converters.get((targetClass, annotations.map(a => a.asInstanceOf[Annotation].annotationType())))
    if (converter.isDefined) converter.get.convert(value, annotations, targetClass)
    else throw new IllegalStateException(s"Primitive Value is not primitive [$value] [Target: $targetClass]")
  }

}

sealed trait PrimitiveTypeConverter {
  def convert: (String, List[_], Class[_]) => Object
}

private case class StringConverter() extends PrimitiveTypeConverter {
  override def convert: (String, List[_], Class[_]) => String = (value, _, _) => value
}

private case class IntegerConverter() extends PrimitiveTypeConverter {
  override def convert: (String, List[_], Class[_]) => Integer = (value, _, _) =>
    if (StringUtils.isEmpty(value)) null else lang.Integer.valueOf(value)
}

private case class LongConverter() extends PrimitiveTypeConverter {
  override def convert: (String, List[_], Class[_]) => lang.Long = (value, _, _) =>
    if (StringUtils.isEmpty(value)) null else lang.Long.valueOf(value)
}

private case class DoubleConverter() extends PrimitiveTypeConverter {
  override def convert: (String, List[_], Class[_]) => lang.Double = (value, _, _) =>
    if (StringUtils.isEmpty(value)) null else lang.Double.valueOf(value)
}

private case class BooleanConverter() extends PrimitiveTypeConverter {
  override def convert: (String, List[_], Class[_]) => lang.Boolean = (value, _, _) => value match {
    case "1" | "true" => lang.Boolean.TRUE
    case _ => lang.Boolean.FALSE
  }
}

private case class EnumConverter() extends PrimitiveTypeConverter {
  override def convert: (String, List[_], Class[_]) => Object = (value, _, targetClass) => if (value != null)
    try {
      CmsReflectionUtils.instantiateEnum(targetClass.asInstanceOf[Class[Enum[_]]], value)
    } catch {
      case t: Throwable =>
        println(s"Unable to find enum value $targetClass.$value")
        throw t
    } else {
    throw new IllegalStateException(s"Enum cannot be empty [$value] [Target: $targetClass]")
  }
}

private case class DateTimeConverter(forceUtc: Boolean) extends PrimitiveTypeConverter {
  override def convert: (String, List[_], Class[_]) => ZonedDateTime = (value, _, _) => {
    if (StringUtils.isEmpty(value)) null
    else {
      if (value.size == 8 || value.size == 10) // Assume a date: 20240322 or 2024-03-22
        LocalDate.parse(value, PrimitiveTypeConverter.dateTimeFormatter).atStartOfDay(ZoneId.from(ZoneOffset.UTC))
      else if (!forceUtc || value.contains("T") && value.contains("Z")) {
        ZonedDateTime.parse(value, PrimitiveTypeConverter.dateTimeFormatter)
      } else {
        ZonedDateTime.parse(if (value.contains(" ")) value.replace(" ", "T") + "Z" else value + "T00:00:00.000000Z", DateTimeFormatter.ISO_ZONED_DATE_TIME)
      }
    }
  }
}

private case class FormattedConverter() extends PrimitiveTypeConverter {

  override def convert: (String, List[_], Class[_]) => String = (value, annotations, _) => try
    annotations.head.asInstanceOf[Formatted].formatting() match {
      case NoFormatting => convertString(value)
      case NewLineToBR => convertString(value).replace("\r", "<br />\r")
      case HtmlToTags => value

    }
  catch {
    case _: Exception => value
  }

  private def convertString(value: String): String = {
    value.replace("&", "&amp;").replace("'", "&#039;").replace("\"", "&quot;")
  }

}

private case class NumberCsvConverter()() extends PrimitiveTypeConverter {
  override def convert: (String, List[_], Class[_]) => String = (value, _, _) => {
    if (StringUtils.isEmpty(value)) value
    else {
      var csv = value.replaceAll("([^(\\d|,)]+)", ",")
      if (csv.startsWith(",")) csv = csv.substring(1)
      if (csv.endsWith(",")) csv = csv.substring(0, csv.length - 1)
      csv
    }
  }
}