package controllers

import javax.inject.{Inject, Singleton}

import org.webjars.WebJarAssetLocator
import play.api.http.{LazyHttpErrorHandler, HttpErrorHandler}
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment}

import scala.util.matching.Regex

// Copy/pasted from webjars-play 2.4.0-2, since it seems like it doesn't play nicely with Play's auto-reloader if it's part of a library

/**
 * A Play framework controller that is able to resolve WebJar paths.
 * <p>org.webjars.play.webJarFilterExpr can be used to declare a regex for the
 * files that should be looked for when searching within WebJars. By default
 * all files are searched for.
 */
@Singleton
class OurWebJarAssets @Inject() (errorHandler: HttpErrorHandler, configuration: Configuration, environment: Environment) extends AssetsBuilder(errorHandler) {

  val WebjarFilterExprDefault = """.*"""
  val WebjarFilterExprProp = "org.webjars.play.webJarFilterExpr"

  lazy val webJarFilterExpr = configuration.getString(WebjarFilterExprProp).getOrElse(WebjarFilterExprDefault)

  val webJarAssetLocator = new WebJarAssetLocator(
    WebJarAssetLocator.getFullPathIndex(
      new Regex(webJarFilterExpr).pattern, environment.classLoader))

  /**
   * Controller Method to serve a WebJar asset
   *
   * @param file the file to serve
   * @return the Action that serves the file
   */
  def at(file: String): Action[AnyContent] = {
    this.at("/" + WebJarAssetLocator.WEBJARS_PATH_PREFIX, file)
  }

  /**
   * Locate a file in a WebJar
   *
   * @example Passing in `jquery.min.js` will return `jquery/1.8.2/jquery.min.js` assuming the jquery WebJar version 1.8.2 is on the classpath
   *
   * @param file the file or partial path to find
   * @return the path to the file (sans-the webjars prefix)
   *
   */
  def locate(file: String): String = {
    webJarAssetLocator.getFullPath(file).stripPrefix(WebJarAssetLocator.WEBJARS_PATH_PREFIX + "/")
  }

  /**
   * Locate a file in a WebJar
   *
   * @param webjar the WebJar artifactId
   * @param file the file or partial path to find
   * @return the path to the file (sans-the webjars prefix)
   *
   */
  def locate(webjar: String, file: String): String = {
    webJarAssetLocator.getFullPath(webjar, file).stripPrefix(WebJarAssetLocator.WEBJARS_PATH_PREFIX + "/")
  }

  /**
   * Get the full path to a file in a WebJar without validating that the file actually exists
   *
   * @example Calling fullPath("react", "react.js") will return the full path to the file in the WebJar because react.js exists at the root of the WebJar
   *
   * @param webjar the WebJar artifactId
   * @param path the full path to a file in the WebJar
   * @return the path to the file (sans-the webjars prefix)
   *
   */
  def fullPath(webjar: String, path: String): String = {
    val version = webJarAssetLocator.getWebJars.get(webjar)
    s"$webjar/$version/$path"
  }

}

object OurWebJarAssets extends OurWebJarAssets(LazyHttpErrorHandler, play.api.Play.current.configuration,
  Environment.simple(path = play.api.Play.current.path, mode = play.api.Play.current.mode))