// Copyright 2015 Background Signal Pty Ltd

package fender

import anodyne.Matching._
import anodyne.Catching._
import anodyne.BoundedStream.readBounded

import javax.servlet.http.{HttpServletRequest}
import scala.collection.JavaConversions._
import scala.io.Codec

trait Requests {

  type Parameters = scala.collection.mutable.Map[String, Array[String]]

  val PathInfo = extractor {
    request: HttpServletRequest => Option(request.getPathInfo)
  }

  val Path = seqExtractor {
    request: HttpServletRequest => Option(request.getPathInfo) map { s => (s split '/' drop 1).toSeq}
  }

  object Params extends Extractor[HttpServletRequest, Parameters]  {
    def apply( params: (String, Seq[String])*) = {
      val query = (for((n, vs) <- params; v <- vs) yield n + "=" + v) mkString ("?", "&", "")
      if( query == "?") "" else query
    }
    def unapply( request: HttpServletRequest) =
      Some(request.getParameterMap.asInstanceOf[java.util.Map[String, Array[String]]]: Parameters)
  }

  case class StringParam(name: String) extends SeqExtractor[Parameters, String] {
    def apply(values: String*) =
      name -> values
    def unapplySeq(params: Parameters) =
      params get name map { ss => ss: Seq[String] }
//    def unapplySeq(request: HttpServletRequest) =
//      Option(request.getParameterValues(name)) map (_.toSeq)
    def in(request: HttpServletRequest) =
      name -> (Option(request.getParameterValues(name)) map (_.toSeq) getOrElse Seq())
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

  implicit class RichRequest(inner: HttpServletRequest) {
    def apply(name: String) = Option(inner.getParameter(name))
    def params: Parameters = inner.getParameterMap.asInstanceOf[java.util.Map[String, Array[String]]]
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
