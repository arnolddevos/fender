package fender

import org.eclipse._
import jetty.server.Request
import jetty.util.log.{Log, Logger}

trait Logging {
  def logger: Logger = Log.getRootLogger
  def logRequest( request: Request): Unit = logger info logFormat(request)
  def logFormat( request: Request): String = {
    val response = request.getResponse
    import request.{getMethod, getRequestURI, getQueryString, getTimeStamp, getContentLength}
    import response.{getContentCount, getStatus}
    val query = if(getQueryString != null)  "?" + getQueryString else ""
    val elapsed = System.currentTimeMillis - getTimeStamp
    s"$getMethod $getRequestURI$query $getStatus $getContentLength/$getContentCount $elapsed"
  }
}
