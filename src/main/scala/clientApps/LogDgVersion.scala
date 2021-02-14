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
object LogDgVersion extends App:

  val logDgClientVersionZ: ZIO[DgHandle with Console with Clock, DgError, Unit] = for {
    dgClient  <- getDgClient
    dgVersion <- Task(dgClient.checkVersion.getTag) orElse IO.fail(DgErr("getting dgraph version failed. can't communicate to the db probably."))
    _         <- consoleLog(s"Found Dgraph version ${dgVersion}")
  } yield ()

  val logDgClientVersionHZ = logDgClientVersionZ.catchAll(e => consoleLog(e.toString))
  
  def run(args: List[String]) = 
    logDgClientVersionHZ.
    provideCustomLayer(layers.l2).exitCode
