package transport

import java.util.concurrent.ConcurrentLinkedQueue

import scala.concurrent._
import scala.util._

private[transport] class QueueablePromise[T] extends Promise[T] {
  private val p = Promise[T]()
  private val queue = new ConcurrentLinkedQueue[T => _]()
  
  def future: Future[T] = p.future
  def isCompleted: Boolean = p.isCompleted
  def tryComplete(result: Try[T]): Boolean = {
    val completed = p.tryComplete(result)
    if(completed) {
      dequeueIfSuccess()
    }
    completed
  }
  
  def queue[U](f: (T) => U)(implicit ec: ExecutionContext): Unit = {
    queue.add(f)
    dequeueIfSuccess()
  }
  
  private def dequeueIfSuccess(): Unit = {
    p.future.value match {
      case Some(Success(t)) =>
        var f: (T) => _ = null
        while({ f = queue.poll(); f != null }) {
          f(t)
        }
      case _ => ()
    }
  }
}

private[transport] object QueueablePromise {
  def apply[T]() = new QueueablePromise[T]()
}
