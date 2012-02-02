package test.scala

// ���Զ�˲�����perfect����ʱ������
object MultiCore {

  def perfect(n: Int) = 2 * n == (1 to n filter (n % _ == 0) sum)
  val ns = 33550330 to 33550350 // 33550336 is a perfect number

  // ������
  def test1 = {
    val t0 = System.currentTimeMillis
    for (i <- ns) {
      println("start " + i)
      if (perfect(i)) println(i)
    }
    println(System.currentTimeMillis - t0)
  }

  // 21��actor����
  def test2 = {
    import actors.Actor, actors.Actor._
    // ��¼�����actor
    val t0 = System.currentTimeMillis

    object Record extends Actor {
      var count = 0
      def act = loop {
        react {
          case 1 => {
            count += 1
            if (count == ns.size) {
              println(System.currentTimeMillis - t0)
              exit
            }
          }
        }
      }
    }
    Record.start

    // �����actor���д���
    for (i <- ns) {
      actor {
        react {
          case n: Int =>
            println("start " + n)
            if (perfect(n)) println(n)
            Record ! 1
        }
      } ! i
    }
  }

  // 2��actor����
  def test5 = {
    import actors.Actor, actors.Actor._
    // ��¼�����actor
    val t0 = System.currentTimeMillis
    class A extends Actor {
      def react1 = react {
        case n: Int => {
          println("start " + n)
          if (perfect(n)) println(n)
          if (n == ns.last || (n + 1) == ns.last) {
            println(System.currentTimeMillis - t0)
            exit
          }
        }
      }
      def act = loop { react1 }
    }
    // �����actor���д���  
    val a1 = (new A).start
    val a2 = (new A).start
    for (i <- ns) {
      if (i % 2 == 0) a1 ! i else a2 ! i
    }
  }

  // Java Thread
  def test3 = {

    import java.util.concurrent.CountDownLatch
    val countdown = new CountDownLatch(ns.size); // �ȴ������߳̽�����1��

    val t0 = System.currentTimeMillis
    for (i <- ns) {
      val t = new Thread {
        override def run = {
          println("start " + i)
          if (perfect(i)) println(i)
          countdown.countDown() // �ȴ������߳̽�����2��
        }
      }
      t.start
    }
    countdown.await() // �ȴ������߳̽�����3��
    println(System.currentTimeMillis - t0)
  }

  // Java Thread Pool
  def test4 = {

    import java.util.concurrent.CountDownLatch
    val countdown = new CountDownLatch(ns.size); // �ȴ������߳̽�����1��

    class T1(n: Int) extends Runnable {
      override def run = {
        println("start " + n)
        if (perfect(n)) println(n)
        countdown.countDown()
      }
    }

    import java.util.concurrent.Executors
    val pool = Executors.newFixedThreadPool(2)

    val t0 = System.currentTimeMillis
    for (i <- ns) {
      val t = new T1(i)
      pool.execute(t)
    }

    countdown.await()
    pool.shutdown
    println("time=" + (System.currentTimeMillis - t0))
  }

  def main(args: Array[String]): Unit = {
    def usage = {
      println("usage MultiCore 1|2")
      exit
    }
    if (args.length == 0) usage
    args(0) match {
      case "1" => test1
      case "2" => test2
      case "3" => test3
      case "4" => test4
      case "5" => test5
      case _ => usage
    }
  }
}
