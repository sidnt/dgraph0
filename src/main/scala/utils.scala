package dgraph0

import zio._
import clock._
import console._

object utils:

  def consoleLog(m:String): ZIO[Clock with Console, Nothing, Unit] = (for {
    time  <- currentDateTime.map(_.toLocalTime)
    _     <- putStrLn(s"[$time] $m")
  } yield ()) orElse putStrLn(s"consoleLog($m) failed")
