package clientApps.messageRegistry

import clientApps._

object StoreHelloWorld extends App {

  val storeHelloWorld = storeMessageInDgraph("hello world").provideCustomLayer(layers.l2).exitCode

  def run(args: List[String]) = storeHelloWorld

}