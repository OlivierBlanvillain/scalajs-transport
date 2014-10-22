package transport.akka

import scala.scalajs.js

import org.scalajs.spickling._
import org.scalajs.spickling.jsany._

trait Serializer extends AbstractSerializer {
  type PickleType = js.Any
  implicit protected def pickleBuilder: PBuilder[PickleType] = JSPBuilder
  implicit protected def pickleReader: PReader[PickleType] = JSPReader
  def parse(s: String): PickleType = js.JSON.parse(s)
  def stringify(p: PickleType): String = js.JSON.stringify(p)
}
