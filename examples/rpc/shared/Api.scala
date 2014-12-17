package shared

trait Api {
  def list(path: String): Seq[String]
  def double(i: Int): Int
}
