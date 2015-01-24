package test

import org.scalatest._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import transport._

class TestConnectionUtils extends FlatSpec with Matchers {
  val sentMessage = "lol"
  
  "dummyConnectionPair" should "be connected" in {
    val promise = Promise[String]()
    val (connection1, connection2) = ConnectionUtils.dummyConnectionPair()
    connection2.handlerPromise.success(promise.success)
    connection1.write(sentMessage)

    assert(sentMessage == Await.result(promise.future, 1.seconds))
  }
  
  "forked connection" should "exchange message" in {
    val pr1a = Promise[String]()
    val pr1b = Promise[String]()
    val pr2a = Promise[String]()
    val pr2b = Promise[String]()
  
    val (base1, base2) = ConnectionUtils.dummyConnectionPair()
    val (fork1a, fork1b) = ConnectionUtils.fork(base1)
    val (fork2a, fork2b) = ConnectionUtils.fork(base2)
    
    List((fork1a, pr1a), (fork1b, pr1b), (fork2a, pr2a), (fork2b, pr2b)) foreach { case (f, p) =>
      f.write(sentMessage)
      f.handlerPromise.success(p success _)
    }
    
    List(pr1a, pr1b, pr2a, pr2b) foreach { p =>
      assert(sentMessage == Await.result(p.future, 1.seconds))
    }
  }
}
