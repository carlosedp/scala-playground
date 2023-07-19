// Run with scala-cli producerconsumer.scala

//> using scala "3.3.0-RC3"
//> using lib "dev.zio::zio:2.0.13"
//> using lib "dev.zio::zio-kafka:2.2"
//> using lib "dev.zio::zio-logging:2.1.12"
//> using lib "dev.zio::zio-logging-slf4j2-bridge:2.1.12"

//> using option "-source:future"

import zio.*
import zio.kafka.consumer.*
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.*
import zio.logging.*
import zio.logging.slf4j.bridge.Slf4jBridge
import zio.stream.ZStream

object MainApp extends ZIOAppDefault:
  val producer: ZStream[Producer, Throwable, Nothing] =
    ZStream
      .repeatZIO(Random.nextIntBetween(0, Int.MaxValue))
      .schedule(Schedule.fixed(2.seconds))
      .mapZIO: random =>
        Producer.produce[Any, Long, String](
          topic = "random",
          key = random % 4,
          value = random.toString,
          keySerializer = Serde.long,
          valueSerializer = Serde.string,
        ).tap(r => Console.printLine(s"Produced value \"$random\" with offset ${r.offset}"))
      .drain

  val consumer: ZStream[Consumer, Throwable, Nothing] =
    Consumer
      .plainStream(Subscription.topics("random"), Serde.long, Serde.string)
      .tap(r => Console.printLine(s"Consumed key: ${r.key}, value: ${r.value}"))
      .map(_.offset)
      .aggregateAsync(Consumer.offsetBatches)
      .mapZIO(_.commit)
      .drain

  def producerLayer =
    ZLayer.scoped(
      Producer.make(
        settings = ProducerSettings(List("localhost:29092")),
      ),
    )

  def consumerLayer =
    ZLayer.scoped(
      Consumer.make(
        ConsumerSettings(List("localhost:29092")).withGroupId("group"),
      ),
    )

  val logFilter: LogFilter[String] = LogFilter.logLevelByName(
    LogLevel.Debug,
    "SLF4J-LOGGER"     -> LogLevel.Warning,
    "org.apache.kafka" -> LogLevel.Warning,
  )

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    Runtime.removeDefaultLoggers >>> consoleLogger(
      ConsoleLoggerConfig(LogFormat.colored, logFilter),
    )

// Both producer and consumer (doesn't interrupt with ctrl-c because of the consumer)
  override def run =
    producer
      .merge(consumer)
      .runDrain
      .provide(
        producerLayer,
        consumerLayer,
        Slf4jBridge.initialize,
      )
