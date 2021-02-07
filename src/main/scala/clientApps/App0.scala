package clientApps

import zio._
import console._
import clock._

import dgraph0._
import dgHandle._
import dgConfig._
import utils._
import DgError._

/** Assuming the database is up:
  * WAP to get DG version and put it out to console
  * .
  * if it is not, the app should put out a one liner saying something went wrong
  */
object App0 extends App:

  val logDgClientVersionZ: ZIO[DgHandle with Console with Clock, DgError, Unit] = for {
    dgClient  <- getDgClient
    _         <- consoleLog(s"domain program begins")
    dgVersion <- Task(dgClient.checkVersion.getTag) orElse IO.fail(DgErr("getting dgraph version failed. can't communicate to the db probably."))
    _         <- consoleLog(s"Found Dgraph version ${dgVersion}")
    _         <- consoleLog(s"domain program ends")
  } yield ()

  val logDgClientVersionHZ = logDgClientVersionZ.catchAll(e => consoleLog(e.toString))

  val l0: ZLayer[Any, Nothing, DgConfig] = defaultDgConfig
  val l1: ZLayer[Any, Nothing, DgConfig with Console with Clock] = defaultDgConfig ++ Console.live ++ Clock.live
  val l2: ZLayer[Any, DgError, DgHandle] = l1 >>> defaultDgHandle
  
  def run(args: List[String]) = 
    logDgClientVersionHZ.
    provideCustomLayer(l2).exitCode
