//> using scala "3.3.0"
//> using lib "dev.zio::zio:2.0.15"

// This option allows assigning directly from a tuple in a for comprehension without a separate `=`
//> using option "-source:future"

import zio.*
import java.io.IOException

/** Custom extension methods for ZIO Effects
  */
extension [R, E, A](z: ZIO[R, E, A])

  /** Measure the execution time for the wrapped effect. Returns a tuple
    * containing the effect result, time in milisseconds and description.
    * {{{
    *   for
    *     res <- my\Effect().measureTime("description")
    *     (e, t, d) = res
    *   ...
    *   yield ()
    * }}}
    *
    * @param description
    *   the description to be returned
    * @return
    *   a tuple containing the effect result, the execution time in milisseconds
    *   and the description.
    */
  def measureTime(description: String = ""): ZIO[R, E, (A, Double, String)] =
    for
      r <- z.timed
      (t, res) = r
    yield (res, t.toNanos / 1000000.0, description)

  /** Measure the execution time for the wrapped effect printing the output to
    * console.
    *
    * @param description
    *   the description to be used in console print
    * @return
    *   the effect result
    */
  def measureTimeConsole(description: String = z.toString): ZIO[R, Any, A] =
    for
      (r, t, d) <- z.measureTime(description)
      // res      <- z.measureTime(description)
      // (r, t, d) = res // Let's not depend on `-source:future`
      _ <- Console.printLine(s" Execution of \"$d\" took $t milis.")
    yield r

/** Receive a string message and print to the console after a random delay
  *
  * @param t
  *   is the message to be printed
  * @return
  */
def printSomethingDelayed(t: String): ZIO[Any, IOException, Unit] =
  for
    delay <- Random.nextIntBetween(10, 500)
    _ <- ZIO.sleep(delay.milliseconds)
    _ <- Console.printLine(s"  Message is \"$t\", delayed by $delay milis")
  yield ()

// Sample messages to be printed with the effect
val doOne = printSomethingDelayed("One")
val doTwo = printSomethingDelayed("Two")
val doThree = printSomethingDelayed("Three")

/** Tries to return a message but might randomly fail. If it fails, retry. A
  * success percent can be passed
  *
  * @param msg
  *   is the message to be printed
  * @param successPercent
  *   is the percentage of success the effect will have. Defaults to 50%.
  * @return
  *   ZIO[Any, Exception, String]
  */
def eventuallyFail(msg: String, successPercent: Int = 50) =
  require(successPercent >= 0 && successPercent <= 100)
  for
    percent <- Random.nextIntBetween(0, 100)
    _ <- ZIO.when(percent > successPercent):
      ZIO.fail(Exception(msg))
    r <- ZIO.succeed(msg)
  yield r

  /** Handle error in executing effect applying a retry policy and reporting
    * result
    * @param effect
    *   is the effect to be run
    * @param retries
    *   is the amount of retries
    * @param backoffTime
    *   is the backoff to apply between each retry
    * @return
    *   the effect result or message with the error
    */
def handleError[R, E, A](
    effect: ZIO[R, E, A],
    retries: Int = 1,
    backoffTime: Int = 2
) =
  // Let's run the effect and treat it with exponential retries in case of error
  val policy =
    (Schedule.exponential(backoffTime.seconds) && Schedule.recurs(retries))
      .tapOutput(o =>
        Console
          .printLine(
            s"↺ Retry ${o._2}, waiting ${o._1.toSeconds()}s for the next try"
          )
          .orDie
      )
  effect
    .map(msg => s"✅ Yes, it worked... message is \"$msg\"")
    .retry(policy)
    .catchAll(e =>
      ZIO.succeed(
        s"💥 Oops... it failed even after $retries retries. Error ($e)"
      )
    )

// Main app
object ZioPlay1 extends ZIOAppDefault:
  val run =
    val retries = 3
    for
      // Start
      _ <- Console.printLine("➡️ Let's run in sequence...")
      // Just test execution time for a printLine
      _ <- Console.printLine("➡️ Test new measure").measureTimeConsole("Test")
      // Run effects in sequence

      res <- (for
        _ <- doOne
        _ <- doTwo
        _ <- doThree
        _ <- handleError(eventuallyFail("Did I work?"), 3).tap(
          Console.printLine(_)
        )
      yield ()).measureTime("sequential run")
      (_, t, d) = res
      _ <- Console.printLine(s" Execution of $d took $t milis.")
      // Test sleep execution time
      _ <- Console.printLine("➡️ Sleep test...")
      e <-
        (ZIO.sleep(1.second) *> ZIO.succeed(
          "Print something from sleep effect"
        )).measureTimeConsole("1 second sleep")
      _ <- Console.printLine(s"Result: $e")
      // Run effects in parallel
      _ <- Console.printLine("➡️ Now in parallel...")
      e <- ZIO
        .collectAllPar(
          List(
            doOne,
            doTwo,
            doThree,
            handleError(eventuallyFail("Did I work in parallel?"), retries)
          )
        )
        .measureTimeConsole("parallel run")
      _ <- Console.printLine(
        e(3)
      ) // Let's get the fourth message from the effect return
    yield ExitCode.success
