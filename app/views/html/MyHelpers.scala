package views.html

import views.html.helper.FieldConstructor

object MyHelpers {
  implicit val bootstrapFields = FieldConstructor(bootstrapFieldConstructor.apply)
}
