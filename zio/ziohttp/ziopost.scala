//> using scala "3.3.0"
//> using lib "dev.zio::zio:2.0.15"
//> using lib "dev.zio::zio-http:3.0.0-RC2"
//> using lib "dev.zio::zio-json:0.6.0"

// This option allows assigning directly from a tuple in a for comprehension without a separate `=`
//> using option "-source:future"
//> using option "-Yretain-trees"
//> using option "-Wunused:all"

import zio.*
import zio.http.*
import zio.http.{Headers, Method, Version, MediaType}
import zio.json.*

case class SampleJSON(
    title: String,
    body: String,
    userID: Int
) derives JsonCodec

object PostClientExample extends ZIOAppDefault:
  val headers = Headers(Header.ContentType(MediaType.application.json))
    ++ Headers(Header.Authorization.Bearer("XYZ"))
    ++ Headers(Header.Host("myhost.com"))

  val mytestJSON = SampleJSON("foo", "bar", 1).toJson

  val request = Request(
    headers = headers,
    method = Method.POST,
    url = URL.decode("https://httpbin.org/anything").toOption.get,
    body = Body.fromString(mytestJSON),
    version = Version.`HTTP/1.1`,
    remoteAddress = Option.empty
  )

  val program =
    for
      res <- Client.request(request)
      data <- res.body.asString
      _ <- Console.printLine(data)
    yield ()

  val run = program.provide(Client.default)
