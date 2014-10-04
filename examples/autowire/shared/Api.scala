package shared

trait Api {
  def list(path: String): Seq[String]
}
