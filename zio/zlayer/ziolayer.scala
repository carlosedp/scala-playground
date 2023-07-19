//> using scala "3.3.0"
//> using lib "dev.zio::zio:2.0.15"

//> using options -Wunused:all -Wvalue-discard -feature -deprecation

import zio.*
import java.io.IOException

object MainLayerApp extends ZIOAppDefault:

  // A Subconfig layer
  case class SubConfig(subVal: String)
  object SubConfig:
    def make: ULayer[SubConfig] = ZLayer.succeed(SubConfig("subVal"))

  // A Config layer that depends on SubConfig
  case class Config(myVal: String)
  object Config:
    def make: ZLayer[SubConfig, Nothing, Config] =
      ZLayer.fromFunction((subConfig: SubConfig) => Config(subConfig.subVal))

  // This effect returns a message and depends on Config layer
  def someEffect(msg: String): ZIO[Config, Nothing, String] =
    for
      config <- ZIO.service[Config]
      res <- ZIO.succeed(s"$msg: ${config.myVal}")
    yield res

  // Our main program which depends on Config layer
  def program: ZIO[Config, IOException, Unit] =
    for
      _ <- Console.printLine("Starting program")
      msg <- someEffect("Message is")
      _ <- Console.printLine(msg)
      _ <- Console.printLine("Ending program")
    yield ()

  def run =
    program
      .provide(
        Config.make,
        SubConfig.make
        // ZLayer.Debug.tree // Uncomment to print the dependency tree
      )
