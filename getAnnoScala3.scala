//> using scala "3.3.0"

// import scala.compiletime.*
import scala.quoted.*
import scala.annotation.StaticAnnotation

// This Macro reads an annotation from a class
inline def sqlNameFor[T]: Option[String] = ${ sqlNameForImpl[T] }
private def sqlNameForImpl[T: Type](using Quotes): Expr[Option[String]] =
  import quotes.reflect.*
  val annot = TypeRepr.of[SqlName]
  TypeRepr
    .of[T]
    .typeSymbol
    .annotations
    .collectFirst:
      case term if term.tpe =:= annot => term.asExprOf[SqlName]
  match
    case Some(expr) => '{ Some($expr.sqlName) }
    case None       => '{ None }

// This Macro reads an annotation from a field
inline def sqlFieldNamesFor[T]: Vector[(String, String)] = ${
  sqlFieldNamesForImpl[T]
}

private def sqlFieldNamesForImpl[T: Type](using
    q: Quotes // must be named!!
): Expr[Vector[(String, String)]] =
  import quotes.reflect.*
  val annot = TypeRepr.of[SqlName].typeSymbol
  val tuples: Seq[Expr[(String, String)]] = TypeRepr
    .of[T]
    .typeSymbol
    .primaryConstructor
    .paramSymss
    .flatten
    .collect:
      case sym if sym.hasAnnotation(annot) =>
        val fieldNameExpr = Expr(sym.name.asInstanceOf[String])
        val annotExpr = sym.getAnnotation(annot).get.asExprOf[SqlName]
        '{ ($fieldNameExpr, $annotExpr.sqlName) }
  val seq: Expr[Seq[(String, String)]] = Expr.ofSeq(tuples)
  '{ $seq.toVector }

//------------------------------------------------------------------------------

class SqlName(val sqlName: String) extends StaticAnnotation

@SqlName("app_user")
case class AppUser(
    id: Long,
    firstName: Option[String],
    @SqlName("last_name")
    lastName: String
)
