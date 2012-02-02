package test.scala

object ThreadTest {

  class Thread1 extends Thread {
    override def run() {
      println("Thread1 start...")
      Thread.sleep(1000)
      println("Thread1 stop....")
    }
  }

  // join()
  def test1() = {

    println("test1() start...")
    val t1 = new Thread1
    t1.start()
    t1.join() // 没有join则马上执行下面的
    println("test1() stop....")
  }

  // 没有join()
  def test2() = {

    println("test1() start...")
    val t1 = new Thread1
    t1.start()
    println("test1() stop....")
  }

  // Executors pool
  def test3() = {

    class Thread2(i: Int) extends Runnable {
      override def run() = {
        val ms = scala.util.Random.nextInt(1000)
        Thread.sleep(ms)
        println(Thread.currentThread.getName + ", " + i + ", sleep:" + ms)
      }
    }

    import java.util.concurrent.Executors
    val pool = Executors.newFixedThreadPool(2)
    for (i <- 1 to 10) {
      val t = new Thread2(i)
      pool.execute(t)
    }
    pool.shutdown
  }

  // Effective Java 2 推荐使用 Executors 而不是 Thread
  def test4 = {
    class Thread2 extends Runnable {
      override def run = {
        println("thread 2 start")
        Thread.sleep(500)
        println("thread 2 end")
      }
    }
    import java.util.concurrent.Executors
    val e = Executors.newSingleThreadExecutor
    e.execute(new Thread2)
    e.shutdown()
    println("main() end")
  }

  //--------------------- format

  
  //--------------------- format

  def main(args: Array[String]): Unit = {
    def usage = { println("usage ThreadTest 1|2|3|4"); exit }
    if (args.length == 0) usage
    args(0) match {
      case "1" => test1
      case "2" => test2
      case "3" => test3
      case "4" => test4
      case _ => usage
    }
  }
}
