package clientApps.messageRegistry

import zio._
import console._

import io.dgraph._
import DgraphProto._

import dgraph0.dgHandle._
import clientApps.layers.l2

/* just in case we want a clean slate to begin with again */
object DropAll extends App:

  val dropAllInDgraph: ZIO[DgHandle & Console, domain.Error, Unit] = for {
    dgClient  <- getDgClient
    _         <- putStrLn("dropping everything (including data and schema) from dgraph")
    operation <- ZIO.effect(Operation.newBuilder().setDropAll(true).build()) orElse IO.fail(domain.Error("operation building failed"))
    _         <- ZIO.effect(dgClient.alter(operation)) orElse IO.fail(domain.Error("alter operation failed"))
    _         <- putStrLn("succeeded in dropping everything from dgraph")
  } yield ()

  def run(args: List[String]) = 
    dropAllInDgraph.
    provideCustomLayer(l2).exitCode
