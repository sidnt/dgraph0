package clientApps

import zio._
import console._

import dgraph0._
import dgClient._
import dgConfig._

/** Assuming the database is up:
  * WAP to get DG version and put it out to console
  * .
  * if it is not, the app should put out a one liner saying something went wrong
  */
object App0 extends App:

  val logDgClientVersionZ = for {
    dgClient <- dgClientAccessor
    _ <- putStrLn(s"Found Dgraph version ${dgClient.checkVersion.getTag}")
  } yield ()

  def run(args: List[String]) = 
    logDgClientVersionZ.
    provideCustomLayer(defaultDgConfig >>> defaultDgClient).
    exitCode
