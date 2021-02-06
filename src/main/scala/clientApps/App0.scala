package clientApps

import zio._
import console._
import clock._

import dgraph0._
import dgHandle._
import dgConfig._
import utils._

/** Assuming the database is up:
  * WAP to get DG version and put it out to console
  * .
  * if it is not, the app should put out a one liner saying something went wrong
  */
object App0 extends App:

  val logDgClientVersionZ: ZIO[Console with Clock with DgHandle, Nothing, Unit] = for {
    dgClient  <- getDgClient
    _         <- consoleLog(s"Found Dgraph version ${dgClient.checkVersion.getTag}")
  } yield ()

  val l0: ZLayer[Any, Nothing, ZEnv with DgConfig] = defaultDgConfig ++ ZEnv.live
  val l1: ZLayer[Console with Clock with DgConfig, DgError, DgHandle] = defaultDgHandle
  val l3: ZLayer[Any, DgError, DgHandle] = l0 >>> l1
  
  def run(args: List[String]) = 
    logDgClientVersionZ.
    provideCustomLayer(l3).
    provideLayer(ZEnv.live).
    exitCode
    // provideLayer(defaultDgConfig ++ ZEnv.live >>> defaultDgHandle).exitCode
