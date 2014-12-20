package transport.rpc

// Shared API
trait Api {
  def doThing(i: Int, s: String): Seq[String]
}
