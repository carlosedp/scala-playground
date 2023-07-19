//> using scala "3.3.0"
//> using file "getAnnoScala3.scala"

@main
def mainAnno: Unit =
  println(sqlNameFor[AppUser]) // Some(app_user)
  println(sqlFieldNamesFor[AppUser]) // Vector((lastName,last_name))
