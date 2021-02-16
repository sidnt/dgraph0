package clientApps.messageRegistry

import zio._
import console._

import io.dgraph._
import DgraphProto._

import dgraph0.dgHandle._

def deleteMessageByUid(uid: String) =
  for {
    dgClient  <- getDgClient
    mu        <- Task {
                      var mu = Mutation.newBuilder().build()
                      mu = Helpers.deleteEdges(mu, uid, "message");
                      mu
                    } orElse IO.fail(domain.Error("mutation building failed"))
    txn       = dgClient.newTransaction()  
    _         <- ZIO.effect{txn.mutate(mu); txn.commit}.ensuring(UIO(txn.discard)) orElse IO.fail(domain.Error("mutation failed"))
  } yield ()

object DeleteMessageByUid extends App {

  val deleteMessageByUid0 = (for {
    uid <- putStrLn("give the correct uid to delete> ") *> getStrLn.orDie
    b   <- doesMessageExist(uid)
    _   <- if (b) IO.unit else IO.fail(domain.Error("message doesn't already exist")) 
    _ <- deleteMessageByUid(uid)
  } yield ()).catchAll(de => putStrLn(de.toString))

  def run(args: List[String]) = deleteMessageByUid0.provideCustomLayer(clientApps.layers.l2).exitCode

}
