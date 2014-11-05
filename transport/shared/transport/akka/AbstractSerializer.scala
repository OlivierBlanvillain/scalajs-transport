package transport.akka

import org.scalajs.spickling._
import scala.concurrent._

trait AbstractSerializer {
  type PickleType
  implicit protected def pickleBuilder: PBuilder[PickleType]
  implicit protected def pickleReader: PReader[PickleType]
  def parse(s: String): PickleType
  def stringify(p: PickleType): String
}
