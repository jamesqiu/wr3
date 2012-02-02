package test.scala

import wr3.text._
import wr3.util._
import scala.io._

/**
 * 测试对Text文件的读写
 * @author jamesqiu 2010-10-11
 */
object Filetxt {

  /**
   * 使用java的方法TextFile
   */
  def java(fname:String) = {

	val f = new LineFilter() {
	  var stop = false
	  override def process(l:String):String = {
	 	  if (stop) {
	 	 	null
	 	  } else {
	 	 	if(l startsWith("---  2")) stop = true
	 		println(l)
	 		l
	 	  }
	  }
	}

    TextFile.create(f).process(fname)
  }

  /**
   * 使用scala自身的fromFile
   */
  def scala(fname:String) = {

	val ll = Source.fromFile(fname).getLines.toList		// 文件所有内容
	val tables = ll.mkString("\n").split("---").tail	// 分隔出102个table

	// 代表一个table的3项要素
	case class t3(title:String, fields:String, comments:String)
	// 代表一个field的7个要素
	case class f7(sn:Int, name:String, id:String, ftype:String, length:Int, point:Int, key:Boolean)
	// 代表一个comment的序号和内容
	case class c2(sn:Int, meta:String)

	import wr3.scala.Tools._

	// 得到所有field
	def to7(fields:String) =
      fields.split("\n").filter { l=> l.left("\t").int(0)>0 }.mkString ("\n")

	// 得到title、fields、comments
	def table(all:String) = {
	  val title = all.left("\n").substring(5).trim
	  val fields = all.between("\n", "--字段描述：").trim
	  val comments = all.right("--字段描述：").trim
	  t3(title, fields, comments)
	}

	println("共有：" + tables.size)

	val s3 = table(tables(0))
	println(s3.title)
	println(to7(s3.fields))
	println(s3.comments)

  }

  // ----------------- main() -----------------//
  /**
   * usage:
   * 	java test.scala.Filetxt WordTest2.doc.txt
   */
  def main(args : Array[String]) : Unit = {

//	java(args(0))
	scala(args(0))

  }
}
