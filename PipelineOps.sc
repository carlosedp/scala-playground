#!/usr/bin/env -S scala-cli shebang
//> using scala "3"

/** This works on Scala 2 while `extension` is Scala 3 only */
// import scala.language.implicitConversions
// implicit class Piper[A](val a: A) {
//     import scala.util.chaining._
//     implicit def |>[B](f: (A) => B): B = a.pipe(f)
// }

/** with extension infix */
// extension [A, B](a: A)
//     infix def |>(f: A => B): B = f(a)
/** or with inline */
extension [A](a: A) inline def |>[B](inline f: A => B): B = f(a)

/** Test functions */
val triple = (x: Int) => 3 * x
val sum = (x: Int) => (y: Int) => x + y
val half = (x: Int) => x / 2

/** Instead of */
val classic = half(sum(triple(3))(2))
println(s"Classic result: $classic")

/** Pipe the functions */
val piped = 3 |> triple |> sum(2) |> half
println(s"Piped result: $piped")

/** or using scala.util.chaining */
import scala.util.chaining.*
val utilpiped = 3 pipe triple pipe sum(2) pipe half // or
val chaining = 3.pipe(triple).pipe(sum(2)).pipe(half)
println(s"Scala util.piped result: $utilpiped")
println(s"Chaining result: $chaining")

/** finally aliasing pipe (just using ||> to avoid conflicts with above
  * examples)
  */
extension [A, B](a: A) inline def ||>(inline f: (A) => B): B = a.pipe(f)

val aliased = 3 ||> triple ||> sum(2) ||> half
println(s"Scala aliased util.piped result: $aliased")
