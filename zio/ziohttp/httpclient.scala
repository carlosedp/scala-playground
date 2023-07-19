//> using scala 3
//> using lib "dev.zio::zio:2.0.15"
//> using lib "dev.zio::zio-http:3.0.0-RC2"

// This option allows assigning directly from a tuple in a for comprehension without a separate `=`
//> using option "-source:future"

import zio.*
import zio.http.Client

object SimpleClient extends ZIOAppDefault:
  val url = "https://sports.api.decathlon.com/groups/water-aerobics"

  val program =
    for
      res  <- Client.request(url)
      data <- res.body.asString
      _    <- Console.printLine(data)
    yield ()

  override val run = program.provide(Client.default)
