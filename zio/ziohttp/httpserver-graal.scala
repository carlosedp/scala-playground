// Run with scala-cli httpserver.scala
// Save logback.xml file into ./resources dir
// Before generating native image binary, run first as above and make a real access to the URL
// so the native-image-agent can generate the metadata in ./resources dir.
// Then generate native image binary with: scala-cli package --native-image httpserver.scala
//> using scala "3.3.0"
//> using lib "dev.zio::zio:2.0.15"
//> using lib "dev.zio::zio-http:3.0.0-RC2"
//> using lib "dev.zio::zio-logging-slf4j2::2.1.13"
//> using lib "ch.qos.logback:logback-classic:1.4.8"

// This runs the app with the native-image-agent to generate the metadata before generating the native image binary
//> using javaOpt -agentlib:native-image-agent=config-merge-dir=resources/META-INF/native-image
//> using resourceDir ./resources

// Optional imports to prevent native-image warnings as described in https://github.com/zio/zio-http/issues/2007
//> using lib "net.jpountz.lz4:lz4:1.3.0"
//> using lib "org.jboss.marshalling:jboss-marshalling:2.1.1.Final"
//> using lib "com.google.protobuf:protobuf-java:2.6.1"

// GraalVM options needed by netty
//> using packaging.graalvmArgs --no-fallback
//> using packaging.graalvmArgs --enable-http
//> using packaging.graalvmArgs --enable-url-protocols=http,https
//> using packaging.graalvmArgs --install-exit-handlers
//> using packaging.graalvmArgs -Djdk.http.auth.tunneling.disabledSchemes=

// Here we ignore the graalvm initialization flags set by the libs themselves
//> using packaging.graalvmArgs --exclude-config .*.jar,.*.properties

package httpserver

import zio.*
import zio.http.*
import zio.logging.backend.SLF4J

object ZIOHTTPServer extends ZIOAppDefault:
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  val app: HttpApp[Any, Nothing] = Http.collectZIO[Request] {
    case Method.GET -> Root / "text" =>
      ZIO.logDebug("Request received") *>
        ZIO.succeed(Response.text("Hello World!"))
  }

  override val run =
    ZIO.logInfo("Server started in http://localhost:8080/text") *>
      Server.serve(app).provide(Server.default)
