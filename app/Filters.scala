import javax.inject.Inject

import play.api.http.HttpFilters
import play.api.mvc.EssentialFilter
import play.filters.csrf.CSRFFilter

class Filters @Inject() (csrfFilter: CSRFFilter) extends HttpFilters {
  override def filters: Seq[EssentialFilter] = Seq(csrfFilter)
}
