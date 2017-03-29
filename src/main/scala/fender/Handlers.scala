package fender

import org.eclipse._
import jetty.server.Handler
import jetty.server.handler.{AbstractHandler, ContextHandler, HandlerCollection, HandlerList, DefaultHandler, ContextHandlerCollection, HandlerWrapper}

trait Handlers { this: Builders with Syntax with Logging =>

  abstract class FenderHandler extends AbstractHandler

  val handlers       = build( new HandlerList )
  val contexts       = build( new ContextHandlerCollection )
  val defaultHandler = build( new DefaultHandler )
  val context        = build( new ContextHandler ) extend config(_.setVirtualHosts(Array[String]()))

  implicit val addFenderHandler = inject[HandlerCollection, FenderHandler] { _ addHandler _ }
  implicit val addContextHandler = inject[ContextHandlerCollection, ContextHandler] { _ addHandler _ }
  implicit val addDefaulttHandler = inject[HandlerCollection, DefaultHandler] { _ addHandler _ }
  implicit val setHandler = inject[HandlerWrapper, HandlerCollection] { _ setHandler _ }

  val path = assign[ContextHandler, String] { _ setContextPath _ }
  val vhost = assign[ContextHandler, String] {
    (target, value) =>
      val hs = target.getVirtualHosts
      target.setVirtualHosts( hs :+ value )
  }
}
