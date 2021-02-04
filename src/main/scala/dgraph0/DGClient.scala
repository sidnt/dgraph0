package dgraph0

import zio._
import io.dgraph._


object dgClient:

  type DgClient = Has[dgClient.Service]

  trait Service:
    val dgraphClient: DgraphClient

  val getDgClient = ZIO.access[DgClient](_.get)
  // val liveDgClient: ZLayer[DGConfig, Throwable, DGClient] = ZLayer.
  // def f(dgcs: dgConfig.Service)
  // val liveDGClient: ZLayer[DGConfig, Throwable, DGClient] = ZLayer.succeed(
  //   val channel: ManagedChannel = ManagedChannelBuilder.forAddress
  //   new DgraphClient(DgraphGrpc.newStub())
  // )