// Copyright 2015 Background Signal Pty Ltd

package fender

import org.eclipse.jetty.server.Response
import scala.util.{Try, Success, Failure}

trait Responses { this: Builders with ContentTypes with Logging =>

  type Content = Config[Response]

  implicit class Entity( ctype: ContentType) {
    def apply( content: Content) = contentType(ctype) andThen content
  }

  val contentType: ContentType => Content = assign { _ setContentType _.mime}

  implicit val text: String => Content = assign { _.getWriter write _ }

  def tryContent[T]( ev: T => Content): Try[T] => Content = {
    case Success(t) => ev(t)
    case Failure(e) => error(e)
  }

  val ok: Content = status(200)

  val status: Int => Content = assign { _ setStatus _ }

  val reset: Content = (_.reset)

  val error: Throwable => Content = (
    e =>
      reset
        andThen status(400)
        andThen contentType(Plain)
        andThen text(e.getMessage)
  )

  val redirect: String => Content = assign {
    (r, url) =>
      r.sendRedirect(r.encodeRedirectURL(url))
  }
}
