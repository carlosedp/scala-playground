// Run with `scli --jmh PipelineBench.scala`, to generate json output, add `-- -rf json` args
//> using scala 3.3.0
//> using options "-Wunused:all"

package bench

import java.util.concurrent.TimeUnit
import scala.util.chaining.*
import org.openjdk.jmh.annotations.*

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 15, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
class PipelineBench:

  @Benchmark
  def classic(): Int =
    half(sum(triple(3))(2))

  @Benchmark
  def infix(): Int =
    3 |> triple |> sum(2) |> half

  @Benchmark
  def inline(): Int =
    3 |>> triple |>> sum(2) |>> half

  @Benchmark
  def utilpiped(): Int =
    3 pipe triple pipe sum(2) pipe half

  @Benchmark
  def utilchaining(): Int =
    3.pipe(triple).pipe(sum(2)).pipe(half)

  @Benchmark
  def aliased(): Int =
    3 ||> triple ||> sum(2) ||> half

/** Test functions */
val triple = (x: Int) => 3 * x
val sum    = (x: Int) => (y: Int) => x + y
val half   = (x: Int) => x / 2

/** with extension infix */
extension [A, B](a: A)
  infix def |>(f:   A => B): B = f(a)

/** or with inline */
extension [A](a:              A)
  inline def |>>[B](inline f: A => B): B = f(a)

/**
 * finally aliasing pipe (just using ||> to avoid conflicts with above examples)
 */
extension [A, B](a: A) inline def ||>(inline f: (A) => B): B = a.pipe(f)

/** Instead of */
// val classic = half(sum(triple(3))(2))
// println(s"Classic result: $classic")

/** Pipe the functions */
// val piped = 3 |> triple |> sum(2) |> half
// println(s"Piped result: $piped")

/** or using scala.util.chaining */
// import scala.util.chaining._
// val utilpiped = 3 pipe triple pipe sum(2) pipe half    // or
// val chaining = 3.pipe(triple).pipe(sum(2)).pipe(half)
// println(s"Scala util.piped result: $utilpiped")
// println(s"Chaining result: $chaining")

// val aliased = 3 ||> triple ||> sum(2) ||> half
// println(s"Scala aliased util.piped result: $aliased")
