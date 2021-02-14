package clientApps

import zio._
import console._
import clock._

import dgraph0._
import dgConfig._
import dgHandle._

object layers {
  val l0: ZLayer[Any, Nothing, DgConfig] = defaultDgConfig
  val l1: ZLayer[Any, Nothing, DgConfig with Console with Clock] = defaultDgConfig ++ Console.live ++ Clock.live
  val l2: ZLayer[Any, DgError, DgHandle] = l1 >>> defaultDgHandle
}
