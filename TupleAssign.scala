//> using scala "3.4.0-RC1-bin-SNAPSHOT"
//> using lib "dev.zio::zio:2.0.15"

//> using options -source:future

import zio.*

@main
def main() =
  // Pure scala:
  def x = for {
    (a, b) <- Some(1, 2)
  } yield (a, b)

  println(x)

  // With ZIO (works):
  def y = for {
    x <- ZIO.fromOption(Some(1, 2))
    (a, b) = x
  } yield (a, b)

  println(y)

  // With ZIO (doesn't work unless using "-source:future"):
  def z = for {
    (a, b) <- ZIO.fromOption(Some(1, 2))
  } yield (a, b)

  println(z)
