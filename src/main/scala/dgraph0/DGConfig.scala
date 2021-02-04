package dgraph0

import zio._

object dgConfig:

  type DGConfig = Has[dgConfig.Service]
  
  trait Service:
    def host = UIO("localhost")
    def port = UIO(9180)

  //accessor
  val dgConfigAccess = ZIO.access[DGConfig](_.get)
