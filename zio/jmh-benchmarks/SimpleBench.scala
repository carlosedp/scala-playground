// Run with `scli --jmh SimpleBench.scala`, to generate json output, add `-- -rf json` args
//> using scala 3.3.0
//> using options "-Wunused:all"

package bench

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
class SimpleBenchmark:

  @Benchmark
  def foo(): Long =
    (1L to 10000000L).sum
