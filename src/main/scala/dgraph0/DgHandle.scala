package dgraph0

import zio._
import console._
import clock._

import io.dgraph._
import io.grpc._
import DgraphGrpc.DgraphStub

import dgConfig._
import DgError._
import utils._

object dgHandle {

  type DgHandle = Has[dgHandle.Service]

  trait Service {

    val dgHandle: (ManagedChannel, DgraphClient)
    /* #doubt how do we enforce constraint here that all instances of dgHandle should honour the constraint
     * that the ManagedChannel instance is the same instance that has been used to create the DgraphClient
     * because that same ManagedChannel instance would be used to close the Db Connection
     * https://github.com/dgraph-io/dgraph4j#closing-the-db-connection
     */ 
  }

  /* using this accessor in domainland, at least we won't be getting unnecessary access to 
   * dgHandle._1 while in the ZManaged block, we can construct and pass the whole DgHandle
   * so as to close the db connection at the end */
  val getDgClient: URIO[DgHandle, DgraphClient] = ZIO.access[DgHandle](_.get.dgHandle._2)

  val f: ZManaged[DgConfig with Clock with Console, DgError, dgHandle.Service] =
    ZManaged.make(
      for {
        dgcs    <-  ZIO.access[DgConfig](_.get)
        _       <-  consoleLog(s"Attempting to build channel")
        channel <-  Task(ManagedChannelBuilder.forAddress(dgcs.host, dgcs.port).usePlaintext().build()) orElse IO.fail(DgErr("stub creation failed"))
        _       <-  consoleLog(s"got channel: $channel")
        _       <-  consoleLog(s"Attempting to build stub")
        stub    <-  Task(DgraphGrpc.newStub(channel)) orElse IO.fail(DgErr("stub creation failed"))
        _       <-  consoleLog(s"got stub: $stub")
        _       <-  consoleLog(s"Attempting to create new DgraphClient")
        dgClient  = new DgraphClient(stub)
        _       <-  consoleLog(s"got DgraphClient: $dgClient")
        dghs    <-  UIO(new dgHandle.Service { val dgHandle = (channel, dgClient) })
      } yield dghs
    )(
      dghs => for {
        _ <- consoleLog(s"attempting to close channel: ${dghs.dgHandle._1}")
        _ <- UIO(dghs.dgHandle._1.shutdown())
        _ <- consoleLog(s"succeeded in closing channel")
      } yield ()
    )

  val defaultDgHandle: ZLayer[DgConfig with Clock with Console, DgError, DgHandle] =
    ZLayer.fromManaged(f)
    
}
