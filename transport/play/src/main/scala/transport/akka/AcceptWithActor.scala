package transport.akka

import akka.actor._

import play.api.libs.json._
import play.api.Play.current
import play.api.libs.concurrent.Akka.system

import org.scalajs.spickling._
import org.scalajs.spickling.playjson._

import transport._
import scala.concurrent._

trait Serializer extends AbstractSerializer {
  type PickleType = JsValue
  implicit protected def pickleBuilder: PBuilder[PickleType] = PlayJsonPBuilder
  implicit protected def pickleReader: PReader[PickleType] = PlayJsonPReader
  def parse(s: String): PickleType = Json.parse(s)
  def stringify(p: PickleType): String = Json.stringify(p)
}
