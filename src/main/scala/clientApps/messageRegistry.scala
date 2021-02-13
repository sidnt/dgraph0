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
 * 2. query messages based on a string
 */

object Cli extends App {

  val getChoice = for {
    choice: String  <- putStr("\nenter\n1. to store a string message.\n2. to query messages based on terms.\nAnything else to exit.\nyour choice> ") *> getStrLn.orDie
    choiceInt       <-  Task { choice.toInt } orElse UIO(-1)
  } yield choiceInt

  def selectZio(i: Int): UIO[ZIO[Console & DgHandle, domain.Error, Unit]] = UIO(
    i match {
      case 1 => StoreUserInputMessage.storeUserInputMessage *> app
      case 2 => QueryMessagesWithTerms.queryMessagesWithTerm *> app
      case _ => ZIO.unit
    }
  )

  val app = for {
    ch <- getChoice
    z <- selectZio(ch)
    _ <- z
  } yield ()

  def run(args: List[String]) = 
    app.
    provideCustomLayer(App0.l2).exitCode

}

 /* just in case we want a clean slate to begin with again */
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

  case class Message(message: String)
  case class QueryMessageResults(queryMessagesResults: List[Message])

  /* this makes it #hardcoded to message schema in the db
   * if the selection of fields returned is changed, the decoding doesn't fail if `uid` is added
   * if `message` is removed, then the decoding fails
   * probably circe is selecting only the fields present and not caring about the extra ones */
  def makeDgQueryString(terms: String) = s"""
  {
    queryMessagesResults(func: anyofterms(message, "$terms")) {
      message
    }
  }
  """

  def getMessagesFromQueryResultString(qrs: String) = {
    import io.circe._
    import parser.decode
    import generic.auto._
    val x = decode[QueryMessageResults](qrs)
    x.map(qmr => qmr.queryMessagesResults.map(m => m.message).mkString("\n"))
    /* ^ just takes the QueryMessageResults case class and makes a printable sequence of messages out of it */
  }

  /* For read-only transactions, there is no need to call Transaction.discard, which is equivalent to a no-op. */
  val queryMessagesWithTerm = for {
    dgClient        <- getDgClient
    queryTerms      <- putStr("\nenter terms (separated by whitespace) you want to search messages with\nyour input> ") *> getStrLn.orDie
    dgQueryString   = makeDgQueryString(queryTerms)
    response        <- Task(dgClient.newReadOnlyTransaction.query(dgQueryString)) orElse IO.fail(domain.Error("readonly transaction failed"))
    stringResponse  = response.getJson.toStringUtf8
    _               <- putStrLn("\ngot response>\n")
    _               <- putStrLn(getMessagesFromQueryResultString(stringResponse).fold(_ => "parsing failed", _.toString))
    _               <- putStrLn("\nbye")
  } yield ()

  

  def run(args: List[String]) = queryMessagesWithTerm.provideCustomLayer(App0.l2).exitCode
  
}
