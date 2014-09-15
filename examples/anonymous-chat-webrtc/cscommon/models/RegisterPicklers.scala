package models

import org.scalajs.spickling._

object RegisterPicklers {
  import PicklerRegistry.register

  register[Msg]
  register[Connected]
    
  def registerPicklers(): Unit = ()
}
