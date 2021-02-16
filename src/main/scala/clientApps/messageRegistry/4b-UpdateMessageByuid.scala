package clientApps.messageRegistry

import zio._
import console._

import io.dgraph._
import DgraphProto._

import io.circe._
import parser.decode
import generic.auto._

import com.google.protobuf.ByteString

import dgraph0.dgHandle._

private def makeSetMessageJsonFrom(uid: String, updatedMessage: String): String = {
  s"""{"uid": "$uid","message": "$updatedMessage"}"""
  // if there's a problem in this string ^ error will be thrown, eg if "$m" was without quotes
}

def updateMessageByUid(uid: String, updatedMessage: String) =
  for {
    dgClient  <- getDgClient
    sj        = makeSetMessageJsonFrom(uid, updatedMessage)
    mu        <- ZIO.effect(Mutation.newBuilder().setSetJson(ByteString.copyFromUtf8(sj)).build) orElse IO.fail(domain.Error("mutation building failed"))
    txn       = dgClient.newTransaction()  
    _         <- ZIO.effect{txn.mutate(mu); txn.commit}.ensuring(UIO(txn.discard)) orElse IO.fail(domain.Error("mutation failed"))
  } yield ()

object UpdateMessageByUid extends App {

  val updateMessageByUid0 = (for {
    uid <- putStrLn("give the correct uid to update> ") *> getStrLn.orDie
    b   <- doesMessageExist(uid)
    _   <- if (b) IO.unit else IO.fail(domain.Error("message doesn't already exist")) 
    msg <- putStrLn("give the updated message to store> ") *> getStrLn.orDie
    _ <- updateMessageByUid(uid, msg)
  } yield ()).catchAll(de => putStrLn(de.toString))

  def run(args: List[String]) = 
    updateMessageByUid0.
    provideCustomLayer(clientApps.layers.l2).exitCode

}
