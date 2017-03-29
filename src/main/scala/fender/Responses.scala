// Copyright 2015 Background Signal Pty Ltd

package fender

import org.eclipse.jetty.server.Response
import scala.util.{Try, Success, Failure}

trait Responses { this: Builders with ContentTypes with Logging =>

  type Content[T] = T => Config[Response]

  implicit class Entity( ctype: ContentType) {
    def apply( body: Config[Response]) = contentType(ctype) andThen body
  }

  val contentType: Content[ContentType] = assign { _ setContentType _.mime}

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
}
