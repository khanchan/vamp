package io.vamp.http_api

import akka.http.scaladsl.model.StatusCodes.{ NotFound, OK }
import akka.util.Timeout
import io.vamp.common.Namespace
import io.vamp.common.http.HttpApiDirectives
import io.vamp.operation.controller.MetricsController

trait MetricsRoute extends AbstractRoute with MetricsController {
  this: HttpApiDirectives ⇒

  def metricsRoutes(implicit namespace: Namespace, timeout: Timeout) = pathPrefix("metrics") {
    get {
      path("gateways" / Segment / Segment) { (gateway, metrics) ⇒
        pathEndOrSingleSlash {
          onSuccess(gatewayMetrics(gateway, metrics)) {
            case Some(result) ⇒ respondWith(OK, result)
            case _            ⇒ respondWith(NotFound, None)
          }
        }
      } ~ path("gateways" / Segment / "routes" / Segment / Segment) { (gateway, route, metrics) ⇒
        pathEndOrSingleSlash {
          onSuccess(routeMetrics(gateway, route, metrics)) {
            case Some(result) ⇒ respondWith(OK, result)
            case _            ⇒ respondWith(NotFound, None)
          }
        }
      } ~ path("deployments" / Segment / "clusters" / Segment / "ports" / Segment / Segment) { (deployment, cluster, port, metrics) ⇒
        pathEndOrSingleSlash {
          onSuccess(clusterMetrics(deployment, cluster, port, metrics)) {
            case Some(result) ⇒ respondWith(OK, result)
            case _            ⇒ respondWith(NotFound, None)
          }
        }
      } ~ path("deployments" / Segment / "clusters" / Segment / "services" / Segment / "ports" / Segment / Segment) { (deployment, cluster, service, port, metrics) ⇒
        pathEndOrSingleSlash {
          onSuccess(serviceMetrics(deployment, cluster, service, port, metrics)) {
            case Some(result) ⇒ respondWith(OK, result)
            case _            ⇒ respondWith(NotFound, None)
          }
        }
      } ~ path("deployments" / Segment / "clusters" / Segment / "services" / Segment / "instances" / Segment / "ports" / Segment / Segment) { (deployment, cluster, service, instance, port, metrics) ⇒
        pathEndOrSingleSlash {
          onSuccess(instanceMetrics(deployment, cluster, service, instance, port, metrics)) {
            case Some(result) ⇒ respondWith(OK, result)
            case _            ⇒ respondWith(NotFound, None)
          }
        }
      }
    }
  }
}
