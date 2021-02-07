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

  val logDgClientVersionZ: ZIO[DgHandle with Console with Clock, Nothing, Unit] = for {
    dgClient  <- getDgClient
    _         <- consoleLog(s"Found Dgraph version ${dgClient.checkVersion.getTag}")
  } yield ()

  val l0: ZLayer[Any, Nothing, DgConfig] = defaultDgConfig
  val l1: ZLayer[Any, Nothing, DgConfig with Console with Clock] = defaultDgConfig ++ Console.live ++ Clock.live
  val l2: ZLayer[Any, DgError, DgHandle] = l1 >>> defaultDgHandle
  
  def run(args: List[String]) = 
    logDgClientVersionZ.
    provideCustomLayer(l2).exitCode
