//> using scala "3.3.0-RC3"
//> using lib "dev.zio::zio:2.0.10"

//> using option "-source:future"

import zio.*

object ZioRefs extends ZIOAppDefault:
  val run =
    for
      ref   <- Ref.make(0)
      left  <- ref.updateAndGet(_ + 1).debug("left").fork
      right <- ref.updateAndGet(_ + 1).debug("right").fork
      _     <- left.join
      _     <- right.join
      _     <- Console.printLine(s"Result: ${ref}")
    yield ()
