package io.vamp.model.artifact

import io.vamp.model.reader.Percentage

import scala.language.implicitConversions

object Gateway {

  val anonymous = ""

  object Sticky extends Enumeration {
    val Service, Instance = Value

    def byName(sticky: String): Option[Sticky.Value] = Gateway.Sticky.values.find(_.toString.toLowerCase == sticky.toLowerCase)
  }

}

case class Gateway(name: String, port: Port, sticky: Option[Gateway.Sticky.Value], routes: List[Route], active: Boolean = false) extends Artifact with Lookup {
  def routeBy(path: GatewayPath) = routes.find(_.path == path)

  def inner = routes.forall(_.length == 4)
}

object GatewayPath {

  val pathDelimiter = "/"

  private val pathDelimiterSplitter = "\\/"

  def apply(source: String) = string2path(source)

  def apply(path: List[Any] = Nil) = list2path(path.map(_.toString))

  implicit def string2path(source: String): GatewayPath = new GatewayPath(source, source.split(pathDelimiterSplitter).toList.filterNot(_.isEmpty))

  implicit def list2path(path: List[String]): GatewayPath = new GatewayPath(path.mkString(pathDelimiter), path.filterNot(_.isEmpty))
}

case class GatewayPath(source: String, segments: List[String]) {

  val normalized = segments.mkString(GatewayPath.pathDelimiter)

  override def equals(obj: scala.Any): Boolean = obj match {
    case routePath: GatewayPath ⇒ segments == routePath.segments
    case _                      ⇒ super.equals(obj)
  }
}

object Route {
  val noPath = GatewayPath()
}

sealed trait Route extends Artifact {
  def path: GatewayPath

  val length = path.segments.size
}

sealed trait AbstractRoute extends Route {

  def weight: Option[Percentage]

  def filters: List[Filter]

  def hasRoutingFilters: Boolean = filters.exists(DefaultFilter.isRouting)
}

case class RouteReference(name: String, path: GatewayPath) extends Reference with Route

case class DefaultRoute(name: String, path: GatewayPath, weight: Option[Percentage], filters: List[Filter]) extends AbstractRoute

object DeployedRoute {
  def apply(route: AbstractRoute, targets: List[DeployedRouteTarget]): DeployedRoute = new DeployedRoute(route.name, route.path, route.weight, route.filters, targets)
}

case class DeployedRoute(name: String, path: GatewayPath, weight: Option[Percentage], filters: List[Filter], targets: List[DeployedRouteTarget]) extends AbstractRoute

object DeployedRouteTarget {

  val host = "0.0.0.0"

  def apply(name: String, port: Int) = new DeployedRouteTarget(name, host, port)
}

case class DeployedRouteTarget(name: String, host: String, port: Int) extends Artifact with Lookup

sealed trait Filter extends Artifact

case class FilterReference(name: String) extends Reference with Filter

object DefaultFilter {

  val rewrite = "^(?i)rewrite (.+) if (.+)$".r

  def isHttp(filter: Filter): Boolean = filter.isInstanceOf[DefaultFilter]

  def isRewrite(filter: Filter): Boolean = filter match {
    case f: DefaultFilter ⇒ f.condition match {
      case rewrite(n, c) ⇒ true
      case any           ⇒ false
    }
    case _ ⇒ false
  }

  def isRouting(filter: Filter) = !isRewrite(filter)
}

case class DefaultFilter(name: String, condition: String) extends Filter
