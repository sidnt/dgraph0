package clientApps.messageRegistry

import zio._
import console._

import io.circe._
import parser.decode
import generic.auto._

import dgraph0.dgHandle._

def makeDgQueryString(uid: String) =
s"""
{
  queryMessagesResults(func: uid($uid)) {
    uid,
    message
  }
}
"""

def doesMessageExistIn(qrs: String): Boolean = decode[QueryMessageResults](qrs).fold(_ => false, _ => true)

def doesMessageExist(uid: String) = 
for {
  /* For read-only transactions, there is no need to call Transaction.discard, which is equivalent to a no-op. */
  dgQueryString   <- UIO(makeDgQueryString(uid))
  dgClient        <- getDgClient
  response        <- Task(dgClient.newReadOnlyTransaction.query(dgQueryString)) orElse IO.fail(domain.Error("readonly transaction failed"))
  stringResponse  = response.getJson.toStringUtf8
  b               = doesMessageExistIn(stringResponse)
} yield b

object CheckMessageExists extends App {

  val checkMessageExists = for {
    uid <- putStr("Enter uid to check> ") *> getStrLn
    b   <- doesMessageExist(uid)
    _   <- putStrLn(b.toString)
  } yield ()

  def run(args: List[String]) = checkMessageExists.provideCustomLayer(clientApps.layers.l2).exitCode
}
