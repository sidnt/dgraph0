package dgraph0

import zio._

object dgConfig:

  type DgConfig = Has[dgConfig.Service]
  
  trait Service:
    val host: String
    val port: Int

  val defaultDgConfig: ZLayer[Any, Nothing, DgConfig] = ZLayer.succeed(
    new Service {
      val host = "localhost"
      val port = 9080
    }
  )
