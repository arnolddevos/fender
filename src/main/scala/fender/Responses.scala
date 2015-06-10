// Copyright 2015 Background Signal Pty Ltd

package fender

import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal
import scala.concurrent.{Future, ExecutionContext}

import org.eclipse.jetty.continuation.Continuation
import org.eclipse.jetty.server.Response

trait Responses extends Builders with ContentTypes {

  type Content[T] = T => Config[Response]

  implicit val contentType: Content[ContentType] = assign { _ setContentType _.mime}

  implicit val text: Content[String] = assign { _.getWriter write _ }

  def tryContent[T]( ev: Content[T]): Content[Try[T]] = {
    case Success(t) => ev(t)
    case Failure(e) => error(e)
  }

  val ok: Config[Response] = pass

  val status: Content[Int] = assign { _ sendError _ }

  val error: Content[Throwable] = {
    t =>
      logger warn t
      status(500) andThen contentType(Plain) andThen text(t.toString)
  }

  val redirect: Content[String] = assign {
    (r, url) => 
      r.sendRedirect(r.encodeRedirectURL(url))
  }

  def runFuture(fr: Future[Config[Response]])(implicit ex: ExecutionContext) = config[Continuation] {
    d =>
      fr onComplete {
        case Success(cfr) => complete(cfr).affect(d)
        case Failure(e)   => complete(error(e)).affect(d)
      }
  }

  val complete: Config[Response] => Config[Continuation] = 
    cfr => config {
      d =>
        try {
          cfr.affect(d.getServletResponse.asInstanceOf[Response])
          d.complete
        }
        catch {
          case NonFatal(e) => logger warn "response abandoned during output: " + e.toString
        }
    }
}
