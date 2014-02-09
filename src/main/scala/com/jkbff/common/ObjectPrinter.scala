package com.jkbff.common

import com.jkbff.common.Helper._
import java.lang.reflect.Field
import scala.annotation.tailrec

class ObjectPrinter {
	val prefixInc = "  "
	
	def printObj(obj: Any, showTypes: Boolean): String = {
		printObj("obj", obj, "", showTypes)
	}
	
	def getObjType(obj: Any): String = {
		obj.getClass.getName().
			replaceAll("java\\.lang\\.(String|Integer|Long|Double|Float|Char|Byte|Short)", "$1")
	}
	
	def getValue(obj: Any, prefix: String, showTypes: Boolean): String = {
		if (obj.isInstanceOf[String] ||
				obj.isInstanceOf[Int] ||
				obj.isInstanceOf[Long] ||
				obj.isInstanceOf[Double] ||
				obj.isInstanceOf[Float] ||
				obj.isInstanceOf[Short] ||
				obj.isInstanceOf[Byte] ||
				obj.isInstanceOf[Char]) {
			obj + "\n"
		} else if (obj.isInstanceOf[Map[_,_]]) {
			"\n" + obj.asInstanceOf[Map[_,_]].foldLeft("") { case (str, (key, value)) =>
				str + printObj(s"[${key}]", value, prefix + prefixInc, showTypes)
			}
		} else if (obj.isInstanceOf[Iterable[_]]) {
			"\n" + obj.asInstanceOf[Iterable[_]].foldLeft("", 0) { case ((str, i), item) =>
				(str + printObj(s"[${i}]", item, prefix + prefixInc, showTypes), i + 1)
			}._1
		} else {
			val fields = getFields(obj)
			if (fields.size == 0) {
				"\n" + prefix + prefixInc + "<no fields>"
			} else {
				"\n" + fields.foldLeft("") { (str, field) =>
					field.setAccessible(true)
					str + printObj(field.getName(), field.get(obj), prefix + prefixInc, showTypes)
				}
			}
		}
	}
	
	def getFields(obj: Any): Array[Field] = {
		getClassHierarchy(obj.getClass).takeWhile(_.getName() != "java.lang.Object").foldLeft(Array[Field]()) { (arr, clazz) =>
			arr ++: clazz.getDeclaredFields()
		}
	}
	
	def printObj(name: String, obj: Any, prefix: String, showTypes: Boolean): String = {
		if (showTypes) {
			prefix + s"${name}: ${getObjType(obj)} = ${getValue(obj, prefix, showTypes)}"
		} else {
			prefix + s"${name} = ${getValue(obj, prefix, showTypes)}"
		}
	}
	
	def getClassHierarchy(clazz: Class[_]): Stream[Class[_]] = clazz #:: getClassHierarchy(clazz.getSuperclass())
}