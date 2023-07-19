// Run with `scli --jmh ZStreamBench.scala`, to generate json output, add `-- -rf json` args
//> using scala 3.3.0
//> using lib "dev.zio::zio:2.0.14"
//> using lib "dev.zio::zio-streams:2.0.14"
//> using lib "dev.zio::zio-profiling-jmh:0.2.0"

//> using options "-Wunused:all", "-Wvalue-discard", "-Wnonunit-statement"

package bench

import zio.*
import zio.stream.*
import zio.profiling.jmh.BenchmarkUtils
// import zio.{Scope as _, *} // Import all from zio and blackhole zio.Scope to avoid name clash with jmh.Scope

import org.openjdk.jmh.annotations.{Scope, *}
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Measurement(iterations = 10, timeUnit = TimeUnit.SECONDS, time = 1)
@Warmup(iterations = 15, timeUnit = TimeUnit.SECONDS, time = 1)
@Threads(1)
class AggregateAsyncBench:
  @Benchmark
  def SingleStreamAggregateAsyncBench(): Unit = executeZio:
    ZStream
      .fromIterable(1 to 100, 10)
      .aggregateAsync(ZSink.foldLeft[Int, Chunk[Int]](Chunk.empty)((chunk, el) => chunk.appended(el)))
      .runForeach(_ => ZIO.sleep(5.millis))

  @Benchmark
  def ManyStreamsAggregateAsyncBench(): Unit = executeZio:
    ZStream
      .fromIterable(sources)
      .flatMapPar(16)(identity)
      .aggregateAsync(ZSink.foldLeft[Int, Chunk[Int]](Chunk.empty)((chunk, el) => chunk.appended(el)))
      .runForeach(_ => ZIO.sleep(5.millis))

  private val sources = (1 to 10).map(_ => ZStream.fromIterable(1 to 100, 10))

  // private def runZio[A](zio: Task[A]): A =
  //   Unsafe.unsafe(implicit unsafe => Runtime.default.unsafe.run(zio).getOrThrow())

  private def executeZio[A](zio: Task[A]): A =
    BenchmarkUtils.unsafeRun(zio)
