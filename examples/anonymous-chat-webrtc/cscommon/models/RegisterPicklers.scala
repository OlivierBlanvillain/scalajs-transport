package models

import org.scalajs.spickling._

object RegisterPicklers {
  import PicklerRegistry.register

  register(YouWillBeCallee)
  register[Message]
    
  def registerPicklers(): Unit = ()
}
