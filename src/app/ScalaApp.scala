package app

import wr3.web._

/**
 * 可以在 wr3.scala.Webapp.scala 中包装所有的 wr3.web._ 的调用，使之更符合scala的惯用法。
 * @author jamesqiu 2010-12-1
 */
class ScalaApp {

  var params: Params = _
  var session: Session = _

  
  def index(id:String) = {
    val xml = <div><h1 style="font: 16px Arial bold">使用Scala的生成xhtml</h1><a href="./a1">a1</a></div>
    Render.html(xml.toString) 
  }
    
  /**
   * url: /wr3/ScalaApp/a1?age=30
   * @param age
   * @return 
   */
  def a1(age: Int) = Render.body(
    "使用Scala写的Controller, age=" + age +
    ".<br/>params=" + params +
    ".<br/>session=" + session)

}