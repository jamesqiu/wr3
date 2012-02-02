package test.scala

import wr3.text._
import wr3.util._
import scala.io._

/**
 * ���Զ�Text�ļ��Ķ�д
 * @author jamesqiu 2010-10-11
 */
object Filetxt {

  /**
   * ʹ��java�ķ���TextFile
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
   * ʹ��scala�����fromFile
   */
  def scala(fname:String) = {

	val ll = Source.fromFile(fname).getLines.toList		// �ļ���������
	val tables = ll.mkString("\n").split("---").tail	// �ָ���102��table

	// ����һ��table��3��Ҫ��
	case class t3(title:String, fields:String, comments:String)
	// ����һ��field��7��Ҫ��
	case class f7(sn:Int, name:String, id:String, ftype:String, length:Int, point:Int, key:Boolean)
	// ����һ��comment����ź�����
	case class c2(sn:Int, meta:String)

	import wr3.scala.Tools._

	// �õ�����field
	def to7(fields:String) =
      fields.split("\n").filter { l=> l.left("\t").int(0)>0 }.mkString ("\n")

	// �õ�title��fields��comments
	def table(all:String) = {
	  val title = all.left("\n").substring(5).trim
	  val fields = all.between("\n", "--�ֶ�������").trim
	  val comments = all.right("--�ֶ�������").trim
	  t3(title, fields, comments)
	}

	println("���У�" + tables.size)

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
