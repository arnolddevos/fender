package fender

import org.eclipse._
import jetty.server.Handler
import jetty.server.handler.{AbstractHandler, ContextHandler, HandlerCollection, DefaultHandler, ContextHandlerCollection, HandlerWrapper}

trait Handlers extends Builders with Syntax with Logging {

  val handlers       = build( new HandlerCollection )
  val contexts       = build( new ContextHandlerCollection )
  val defaultHandler = build( new DefaultHandler )
  val context        = build( new ContextHandler ) extend config(_.setVirtualHosts(Array[String]()))
  val theseHandlers  = pass[HandlerCollection]

  implicit val addHandler = inject[HandlerCollection, Handler] { _ addHandler _ }
  implicit val setHandler = inject[HandlerWrapper, Handler] { _ setHandler _ }

  val path = assign[ContextHandler, String] { _ setContextPath _ }
  val vhost = assign[ContextHandler, String] {
    (target, value) =>
      val hs = target.getVirtualHosts
      target.setVirtualHosts( hs :+ value )
  }
}
