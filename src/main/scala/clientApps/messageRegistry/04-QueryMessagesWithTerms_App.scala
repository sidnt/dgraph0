package clientApps.messageRegistry

import zio._
import console._

import io.circe._
import parser.decode
import generic.auto._

import dgraph0.dgHandle._

object QueryMessagesWithTerms extends App {

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

  def parseMessagesFromQueryResultString(qrs: String): Either[Error, String] = {
    
    val x = decode[QueryMessageResults](qrs)
    x.map(qmr => qmr.queryMessagesResults.map(m => m.message).mkString("\n"))
    /* ^ just takes the QueryMessageResults case class and makes a printable sequence of messages out of it */
  }

  /* For read-only transactions, there is no need to call Transaction.discard, which is equivalent to a no-op. */
  val queryMessagesWithTerm = for {
    queryTerms      <- putStr("\nenter terms (separated by whitespace) you want to search messages with\nyour input> ") *> getStrLn.orDie
    dgQueryString   = makeDgQueryString(queryTerms)
    dgClient        <- getDgClient
    response        <- Task(dgClient.newReadOnlyTransaction.query(dgQueryString)) orElse IO.fail(domain.Error("readonly transaction failed"))
    stringResponse  = response.getJson.toStringUtf8
    _               <- putStrLn("\ngot response>\n")
    _               <- putStrLn(parseMessagesFromQueryResultString(stringResponse).fold(_ => "parsing failed", _.toString))
  } yield ()

  

  def run(args: List[String]) = queryMessagesWithTerm.provideCustomLayer(clientApps.layers.l2).exitCode
  
}
