package test.scala

import wr3.util._

/**
 * 读取带表格（所有大于6列的），表格前面有表名，后面是表格字段的说明
 * @author jamesqiu 2010-10-7
 */
object WordTest {

  def main(args: Array[String]): Unit = {

    val word = new Word
    try {
      word.open("f:/dev3/WordTest2.doc", false)
      val n = word.tables()
      var nn = 0
      for (i <- 1 to n) {
        word.table(i)
        if (word.cols >= 6) {
          nn += 1
          info(word, nn)
        }
      }
    } catch { case e: Exception => e.printStackTrace }
    finally { word.close }

    // 打印每个表格的信息
    def info(word: Word, i:Int) = {
      val (rows,cols) = (word.rows, word.cols)
      val title = word.findTitle("表结构：")
      val comments = word.findComment("字段描述", rows)
      printf("---%3d: %s\n%s\n--字段描述：\n%s\n\n",
    		  i,
    		  title,
    		  tbody,
    		  fmt(comments.toArray))

      def tr(i:Int) = 1 to cols map {c=>word.cell(i, c)} mkString("\t")
      def tbody = 1 to rows map tr mkString("\n")
      def fmt(comments:Array[Object]) =
    	(for((v,i)<- (comments.zipWithIndex)) yield ("%-2d. %s" format ((i+1), v))) mkString "\n"
    }
  }
}
