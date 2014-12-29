package transport

import scala.concurrent._
import scala.util._

private[transport] class QueueablePromise[T] extends Promise[T] {
  private val p = Promise[T]()
  private var poorMansBuffer: Future[T] = p.future
  
  def future: Future[T] = p.future
  def isCompleted: Boolean = p.isCompleted
  def tryComplete(result: Try[T]): Boolean = p.tryComplete(result)
  
  def queue[U](f: (T) => U)(implicit ec: ExecutionContext): Unit = {
    poorMansBuffer = poorMansBuffer.andThen {
      case Success(t) =>
        f(t)
    }
  }
}

private[transport] object QueueablePromise {
  def apply[T]() = new QueueablePromise[T]()
}
