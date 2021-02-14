package clientApps.messageRegistry

import zio._

import io.dgraph._
import DgraphProto._

import dgraph0.dgHandle._
import clientApps.layers.l2

// this would be run only once to create the schema before any crud
object CreateMessageSchema extends App:

  val createMessageSchemaInDg = for {
    dgClient    <- getDgClient
    schema      <- UIO("message: string @index(term) .")
    operation   <- Task(Operation.newBuilder().setSchema(schema).build()) orElse IO.fail(Error("operation building failed"))
    _           <- Task(dgClient.alter(operation))
  } yield ()

  def run(args: List[String]) = 
    createMessageSchemaInDg.
    provideCustomLayer(l2).exitCode
