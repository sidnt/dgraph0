package clientApps

import zio._
import console._
import io.dgraph._
import DgraphProto._
import com.google.protobuf.ByteString

import dgraph0.dgHandle._

/**
 * sometimes, we need something as simple as a message registry to get started
 * message registry provides only 2 functionalities
 * 1. store a string message
 * 2. query messages based on a string (not yet)
 */

 // just in case we want a clean slate to begin with again
object DropAll extends App:

  val dropAllInDgraph = for {
    dgClient <- getDgClient
    operation <- ZIO.effect(Operation.newBuilder().setDropAll(true).build()) orElse IO.fail(Error("operation building failed"))
    _ <- putStrLn("dropping everything in dgraph")
    _ <- ZIO.effect(dgClient.alter(operation))
    _ <- putStrLn("succeeded in droppping everything from dgraph")
  } yield ()

  def run(args: List[String]) = 
    dropAllInDgraph.
    provideCustomLayer(App0.l2).exitCode

// this would be run only once to create the schema before any crud
object CreateMessageSchema extends App:

  val createMessageSchemaInDg = for {
    dgClient <- getDgClient
    schema <- UIO("message: string @index(exact) .")
    operation <- ZIO.effect(Operation.newBuilder().setSchema(schema).build()) orElse IO.fail(Error("operation building failed"))
    _ <- ZIO.effect(dgClient.alter(operation))
  } yield ()

  def run(args: List[String]) = 
    createMessageSchemaInDg.
    provideCustomLayer(App0.l2).exitCode

// helpers
def makeSettingJsonFrom(m: String) = s"""{"uid": "_:message","message": "$m"}"""
// if there's a problem in this string ^ error will be thrown, eg if "$m" was without quotes

def storeMessageInDgraph(m: String) = for {
  dgClient  <- getDgClient
  txn       = dgClient.newTransaction()
  _         <- putStrLn("attempting to store message> ")
  _         <- putStrLn(m)
  sj        = makeSettingJsonFrom(m)
  mu        <- ZIO.effect(Mutation.newBuilder().setSetJson(ByteString.copyFromUtf8(sj)).build) orElse IO.fail(domain.Error("mutation building failed"))
  _         <- ZIO.effect{txn.mutate(mu); txn.commit}.ensuring(UIO({println("discarding txn");txn.discard})) orElse IO.fail(domain.Error("mutation failed"))
} yield ()

object StoreHelloWorld extends App {

  val storeHelloWorld = storeMessageInDgraph("hello world").provideCustomLayer(App0.l2).exitCode

  def run(args: List[String]) = storeHelloWorld

}

object StoreUserInputMessage extends App {

  val storeUserInputMessage = for {
    _ <- putStrLn("give the message to store> ")
    m <- getStrLn.orDie
    _ <- storeMessageInDgraph(m)
  } yield ()

  def run(args: List[String]) = storeUserInputMessage.provideCustomLayer(App0.l2).exitCode

}

object QueryMessagesWithTerms extends App {

  def makeDgQueryString(terms: String) = s"""
  {
    queryMessages(func: anyofterms(message, "$terms")) {
      message
    }
  }
  """

  /* For read-only transactions, there is no need to call Transaction.discard, which is equivalent to a no-op. */
  val queryMessagesWithTerm = for {
    dgClient <- getDgClient
    queryTerms <- putStrLn("\nenter terms (separated by whitespace) you want to search messages with") *> getStrLn.orDie
    dgQueryString = makeDgQueryString(queryTerms)
    response <- Task(dgClient.newReadOnlyTransaction.query(dgQueryString))
    _ <- putStrLn("got raw response> ")
    _ <- putStrLn(response.getJson.toStringUtf8)
    _ <- putStrLn("")
  } yield ()

  def run(args: List[String]) = queryMessagesWithTerm.provideCustomLayer(App0.l2).exitCode
  
}
