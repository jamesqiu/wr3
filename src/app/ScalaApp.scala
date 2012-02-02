package app

import wr3.web._

/**
 * ������ wr3.scala.Webapp.scala �а�װ���е� wr3.web._ �ĵ��ã�ʹ֮������scala�Ĺ��÷���
 * @author jamesqiu 2010-12-1
 */
class ScalaApp {

  var params: Params = _
  var session: Session = _

  
  def index(id:String) = {
    val xml = <div><h1 style="font: 16px Arial bold">ʹ��Scala������xhtml</h1><a href="./a1">a1</a></div>
    Render.html(xml.toString) 
  }
    
  /**
   * url: /wr3/ScalaApp/a1?age=30
   * @param age
   * @return 
   */
  def a1(age: Int) = Render.body(
    "ʹ��Scalaд��Controller, age=" + age +
    ".<br/>params=" + params +
    ".<br/>session=" + session)

}