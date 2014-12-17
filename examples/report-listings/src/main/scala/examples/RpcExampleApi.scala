package transport.rpc

trait Api {
  def doThing(i: Int, s: String): Seq[String]
}
