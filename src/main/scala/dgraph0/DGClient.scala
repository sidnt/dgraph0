package dgraph0

import zio._
import io.dgraph._
import io.grpc._
import DgraphGrpc.DgraphStub

import dgConfig._
import DgError._

object dgClient:

  type DgClient = Has[dgClient.Service]

  trait Service:
    val dgraphClient: DgraphClient

  val dgClientAccessor: URIO[DgClient, DgraphClient] = ZIO.access[DgClient](_.get.dgraphClient)

  def f(dgcm: DgConfig): IO[DgError, dgClient.Service] = for {

    dgc <-  ZIO.effect {
              val channel: ManagedChannel = ManagedChannelBuilder.forAddress(dgcm.get.host, dgcm.get.port).usePlaintext().build()
              val stub: DgraphStub = DgraphGrpc.newStub(channel) 
              new DgraphClient(stub)
            } orElse IO.fail(ClientCreationFailed) // useless orElse part. 
            // defects not handled at this point yet. if Dgraph is down, Fiber will Fail, orElse part will never be reached

  } yield new dgClient.Service {
                val dgraphClient = dgc
              }

  /**
   * we want to do an effect in the layer construction
   */
  val defaultDgClient: ZLayer[DgConfig, DgError, DgClient] = ZLayer.fromFunctionM(f)
