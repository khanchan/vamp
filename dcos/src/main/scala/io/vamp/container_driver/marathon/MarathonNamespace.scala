package io.vamp.container_driver.marathon

import io.vamp.common.{ Artifact, Lookup, NamespaceProvider }
import io.vamp.common.util.HashUtil
import io.vamp.container_driver.ContainerDriver
import io.vamp.model.artifact._

trait MarathonNamespace {
  this: ContainerDriver with NamespaceProvider ⇒

  private val nameDelimiter = "/"

  private val idMatcher = """^(([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])\\.)*([a-z0-9]|[a-z0-9][a-z0-9\\-]*[a-z0-9])$""".r

  protected def appId(workflow: Workflow): String = {
    s"$nameDelimiter${namespace.name}${nameDelimiter}workflow-${artifactName2Id(workflow)}"
  }

  protected def appId(deployment: Deployment, breed: Breed): String = {
    s"$nameDelimiter${namespace.name}${nameDelimiter}deployment-${artifactName2Id(deployment)}-service-${artifactName2Id(breed)}"
  }

  protected def artifactName2Id(artifact: Artifact): String = artifact.name match {
    case idMatcher(_*) ⇒ artifact.name
    case _ ⇒ artifact match {
      case lookup: Lookup ⇒ lookup.lookupName
      case _              ⇒ HashUtil.hexSha1(artifact.name)
    }
  }

  protected val namespaceConstraint: List[String] = MarathonDriverActor.namespaceConstraint()
}
