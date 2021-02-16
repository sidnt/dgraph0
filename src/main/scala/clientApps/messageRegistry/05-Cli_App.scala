package clientApps

import zio._
import console._
import io.dgraph._
import DgraphProto._
import com.google.protobuf.ByteString

import dgraph0.dgHandle._

import messageRegistry._
/**
 * sometimes, we need something as simple as a message registry to get started
 * message registry provides only 3 functionalities
 * 1. store a string message
 * 2. query messages based on a string
 * 3. delete message by uid
 * .
 * the problem with the approach written underneath is,
 * there's a lot of hardcoding, so if we have to extend the functionality of our program
 * in this manner, we will need to write a lot of boilerplate
 * .
 * instead, a better next approch would be to abstract over what has been hardcoded
 * and make it parametric, so that with function application,
 * we could gather a lot of grounds, in a smaller api surface 
 */

object Cli extends App {

  /* from the console input
   * it creates an integer representing the choice of the user */
  val getChoice =
    for {
      _               <-  putStr("\nenter\n1. to store a new string message.\n2. to query messages based on terms.\n3. to update message by uid\n4. to delete message by uid.\nAnything else to exit.\nyour choice> ")  
      choice: String  <-  getStrLn.orDie
      choiceInt       <-  Task { choice.toInt } orElse (putStrLn("your choice> exit app") *> UIO(-1))
    } yield choiceInt

  def selectZio(i: Int): UIO[ZIO[Console & DgHandle, domain.Error, Unit]] = UIO(
    i match {
      case 1 => StoreUserInputMessage.storeUserInputMessage *> app
      case 2 => QueryMessagesWithTerms.queryMessagesWithTerm *> app
      case 3 => UpdateMessageByUid.updateMessageByUid0 *> app
      case 4 => DeleteMessageByUid.deleteMessageByUid0 *> app
      case _ => ZIO.unit
    }
  )

  val app = for {
    ch  <- getChoice
    z   <- selectZio(ch)
    _   <- z //run the effect
  } yield ()

  def run(args: List[String]) = 
    app.
    provideCustomLayer(layers.l2).exitCode

}
