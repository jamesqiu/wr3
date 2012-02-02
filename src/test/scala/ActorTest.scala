package test.scala

object ActorTest {

  import actors._, actors.Actor._

  class Actor1 extends Actor {
    def act {
      receive {
        case s: String => println("act get " + s)
      }
    }
  }

  // 写法1：使用extends Actor
  def test1() {
    for (i <- 1 to 10) {
      val a1 = (new Actor1).start // start 必须的
      println("send hello" + i)
      a1 ! "hello" + i
    }
  }

  // 写法2：使用actor
  def test2() {
    for (i <- 1 to 10) {
      (actor { react { case s: String => println("get " + s) } }) ! "hello" + i
    }
  }

  // 对muttable、unsynchronized object（可变非同步对象）的存取
  def test3() {

    import actors._, actors.Actor._
    object c1 extends Actor {
      var total = 1000
      def get = {
        Thread.sleep(scala.util.Random.nextInt(100))
        total
      }
      def cut(n: Int) = {
        Thread.sleep(scala.util.Random.nextInt(100))
        total -= n
      }
      def act = {
        loop {
          react {
            case n: Int =>
              if ((get - n) < 0) exit
              println("befer: " + total)
              cut(n)
              println("after: " + total)
            case _ => println("unknow"); exit
          }
        }
      }
    }

    object c2 {
      var total = 1000
      def get = {
        Thread.sleep(scala.util.Random.nextInt(100))
        total
      }
      def cut(n: Int) = {
        val n0 = get
        Thread.sleep(scala.util.Random.nextInt(100))
        total = n0 - n
        println("%d-%d=%d" format (n0, n, total))
      }
    }

    //    c1.start
    //    for (i <- 1 to 11) {
    //      actor { println("request " + i); c1 ! 100 }
    //    }

    for (i <- 1 to 10) {
      actor { println("request " + i); c2.cut(100) }
    }
    Thread.sleep(1000)
    println("剩下的" + c2.get)
  }

  def main(args: Array[String]): Unit = {

    import wr3.scala.Tools._
    if (args.size == 0) usage

    args(0) match {
      case "1" => test1
      case "2" => test2
      case "3" => test3
      case _ => usage
    }
  }
}
