package clientApps.messageRegistry

import zio._
import console._

import io.dgraph._
import DgraphProto._

import com.google.protobuf.ByteString

import dgraph0._
import dgHandle._


private def makeSetMessageJsonFrom(m: String): String = {
  s"""{"uid": "_:message","message": "$m"}"""
  // if there's a problem in this string ^ error will be thrown, eg if "$m" was without quotes
}

def storeMessageInDgraph(m: String): ZIO[DgHandle, domain.Error, Unit] =
  for {
    dgClient  <- getDgClient
    sj        = makeSetMessageJsonFrom(m)
    mu        <- ZIO.effect(Mutation.newBuilder().setSetJson(ByteString.copyFromUtf8(sj)).build) orElse IO.fail(domain.Error("mutation building failed"))
    txn       = dgClient.newTransaction()  
    _         <- ZIO.effect{txn.mutate(mu); txn.commit}.ensuring(UIO(txn.discard)) orElse IO.fail(domain.Error("mutation failed"))
  } yield ()


object StoreUserInputMessage extends App {

  val storeUserInputMessage = for {
    _ <- putStrLn("give the message to store> ")
    m <- getStrLn.orDie
    _ <- storeMessageInDgraph(m)
  } yield ()

  def run(args: List[String]) = storeUserInputMessage.provideCustomLayer(clientApps.layers.l2).exitCode

}
