// Copyright 2015 Background Signal Pty Ltd

package fender

import anodyne.Matching._
import anodyne.Catching._
import anodyne.BoundedStream.readBounded

import javax.servlet.http.{HttpServletRequest}
import scala.collection.JavaConversions._
import scala.io.Codec

trait Requests {

  val PathInfo = extractor {
    request: HttpServletRequest => Option(request.getPathInfo)
  }

  val Path = seqExtractor {
    request: HttpServletRequest => Option(request.getPathInfo) map { s => (s split '/' drop 1).toSeq}
  }

  object Params {
    def apply( params: (String, Seq[String])*) = {
      val query = (for((n, vs) <- params; v <- vs) yield n + "=" + v) mkString ("?", "&", "")
      if( query == "?") "" else query
    }
  }

  case class StringParam(name: String) extends SeqExtractor[HttpServletRequest, String] {
    def apply(values: String*) = name -> values
    def unapplySeq(request: HttpServletRequest) = Option(request.getParameterValues(name))
  }

  def DoubleParam(name: String) = StringParam(name) * DoubleValue
  def LongParam(name: String) = StringParam(name) * LongValue
  def IntParam(name: String) = StringParam(name) * IntValue
  def StringSeqParam(name: String) =  StringParam(name) * Split(",")
  val BBox = StringParam("BBOX").first * Split(",") * DoubleValue

  def entityText( req: HttpServletRequest, limit: Long)( implicit codec: Codec): Either[Throwable, CharSequence] = {
     boxExceptions {
       readBounded(req.getInputStream(), req.getContentLength(), limit)
     }
  }

  trait HttpMethod extends Extractor[HttpServletRequest, HttpServletRequest] with Product {
    def name = productPrefix
    def unapply(r: HttpServletRequest): Option[HttpServletRequest] =
      if(r.getMethod == name) Some(r) else None
  }

  case object GET    extends HttpMethod
  case object POST   extends HttpMethod
  case object PUT    extends HttpMethod
  case object DELETE extends HttpMethod
}
