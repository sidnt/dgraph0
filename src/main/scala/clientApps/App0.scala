package clientApps

import zio._
import console._

import dgraph0.dgClient._
import dgraph0._

/** Assuming the database is up:
  * WAP to get DG version and put it out to console
  */
object App0 extends App:

  def run(args: List[String]) = putStrLn("Hi").exitCode

  // val program: ZIO[DgClient, DGError, Unit] = for
  //   dgClient <- getDgClient
  //   dgVersion = dgClient.checkVersion.getTag
  //   _ <- putStrLn(s"Found Dgraph version $dgVersion")
  // yield ()