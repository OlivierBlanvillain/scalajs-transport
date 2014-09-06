package models

import org.scalajs.spickling._

object RegisterPicklers {
  import PicklerRegistry.register

  register(PeerFound)
  register[Message]
    
  def registerPicklers(): Unit = ()
}
