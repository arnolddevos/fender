
package fender

import org.eclipse.jetty.server.Response
import java.net.URL
import sys.process._
import java.io.File
import anodyne.Matching._

trait Resources extends Builders {

  case class ResourceMatch(prefix: String) extends Extractor[String, Config[Response]] {
    def unapply(path: String) =
      if( isClean(path))
        Option(getClass.getResource(prefix + path)) map (resourceResponse(_, guessType(path)))
      else
        None
  }

  case class FileMatch(prefix: String) extends Extractor[String, Config[Response]] {
    def unapply(path: String) = {
      if( isClean(path)) {
        val f = new File(prefix + path)
        if( f.isFile)
          Some(resourceResponse(f, guessType(path)))
        else
          None
      }
      else
        None
    }
  }

  def resourceResponse(source: ProcessBuilder.Source, contentType: String, age: Int=oneWeek): Config[Response] = config {
    r =>
      r.setHeader("Cache-Control", "max-age="+age+",public")
      r.setContentType(contentType)
      (source #> r.getOutputStream).!
  } 

  private val Ext = ".*[.]([^.]+)".r

  private def oneWeek = 604800

  def guessType(path: String) = path match {
    case Ext("js") => "text/javascript"
    case Ext("html") => "text/html"
    case Ext("css") => "text/css"
    case Ext("csv") => "text/csv"
    case Ext("png") => "image/png"
    case Ext("json") => "application/json"
    case _ => "text/plain"
  }
  
  def isClean(path: String) = ! path.startsWith("../") && ! path.contains("/../") 
}
