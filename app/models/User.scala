package models

import play.api.db.DB
import anorm._
import anorm.SqlParser._

object User {
  def isAuthorized(email: String): Boolean = {
    DB.withConnection(implicit c =>
      SQL("SELECT count(*) FROM users WHERE email = '{email}'").on("email"->email).as(scalar[Long].single) match {
        case 1 => true
        case _ => false
      }
    )
  }
}
