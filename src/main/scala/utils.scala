package dgraph0

import zio._
import clock._
import console._

object utils:

  def consoleLog(m:String) = (for {
    time  <- currentDateTime.map(_.toLocalTime)
    _     <- putStrLn(s"[$time] $m")
  } yield ()) orElse putStrLn(s"consoleLog($m) failed")