package transport

import scala.collection.mutable
import scala.concurrent._
import scala.util._

private[transport] class QueueablePromise[T] extends Promise[T] {
  private val p = Promise[T]()
  private val queue = new mutable.Queue[(T) => _]()
  
  def future: Future[T] = p.future
  def isCompleted: Boolean = p.isCompleted
  def tryComplete(result: Try[T]): Boolean = {
    val completed = p.tryComplete(result)
    if(completed) {
      p.future.value match {
        case Some(Success(t)) =>
          while(queue.nonEmpty) {
            val f = queue.dequeue()
            f(t)
          }
        case _ => ()
      }
    }
    completed
  }
  
  def queue[U](f: (T) => U)(implicit ec: ExecutionContext): Unit = {
    p.future.value match {
      case Some(Success(t)) => f(t)
      case _ => queue.enqueue(f)
    }
  }
}

private[transport] object QueueablePromise {
  def apply[T]() = new QueueablePromise[T]()
}
