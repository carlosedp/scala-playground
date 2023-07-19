// Run with:
// scala-cli --jmh bench1.scala -- -rf json
//> using scala 3
//> using options "-Wunused:all"
//> using lib "org.openjdk.jmh:jmh-core:1.36"

package my_bench

import org.openjdk.jmh.annotations.*

import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(1)
class MyBench:
  val magicNumber: Long = 7

  def getNumber(i: Long): Long =
    i & 0xff

  @Benchmark
  def sumSimple(): Long =
    var sum: Long = 0
    for i <- 0L to 1_000_000L
    do
      var n = getNumber(i)
      if n != magicNumber then sum += n
    sum

  def getNumberOrNull(i: Long): Long | Null =
    val n: Long = i & 0xff
    if n == magicNumber then null else n

  @Benchmark
  def sumNulls: Long =
    var sum: Long = 0
    for i <- 0L to 1_000_000L
    do
      val n = getNumberOrNull(i)
      if n != null then sum += n.nn
    sum

  def getNumberOption(i: Long): Option[Long] =
    val n = i & 0xff
    if n == magicNumber then Some(n) else None

  @Benchmark
  def sumOption: Long =
    var sum: Long = 0
    for i <- 0L to 1_000_000L
    do
      val n: Option[Long] = getNumberOption(i)
      if n.isDefined then sum += n.get
    sum
