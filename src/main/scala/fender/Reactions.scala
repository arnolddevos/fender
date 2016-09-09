package fender

import org.eclipse._
import jetty.server.{Handler, Request, Response}
import jetty.server.handler.AbstractHandler
import jetty.continuation.{Continuation, ContinuationSupport, ContinuationListener}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

trait Reactions extends Builders {

  type Reaction[T] = PartialFunction[Request, Config[T]]

  abstract class FenderHandler extends AbstractHandler

  def respond(reaction: Reaction[Response]) = build[FenderHandler] {
    new FenderHandler {
      def handle( _1: String, base: Request, _3: HttpServletRequest, _4: HttpServletResponse) = {
        val run = reaction.runWith {
          cfr =>
            base.setHandled(true)
            cfr.affect(base.getResponse)
            logRequest(base)
        }
        run(base)
      }
    }
  }

  def react[T](reaction: Reaction[Continuation]) = build[FenderHandler] {
    new FenderHandler {
      def handle( _1: String, base: Request, _3: HttpServletRequest, _4: HttpServletResponse) = {
        val run = reaction.runWith {
          cfc =>
            base.setHandled(true)
            val c = ContinuationSupport.getContinuation(base)
            c addContinuationListener(listener(base))
            c.setTimeout(defaultTimeout)
            c.suspend(base.getResponse)
            cfc.affect(c)
        }
        run(base)
      }
    }
  }

  def defaultTimeout = 15*60*1000l

  private def listener(request: Request) = new ContinuationListener {
    def onTimeout(c: Continuation) {
      logger warn "request timeout expired"
      val response = c.getServletResponse.asInstanceOf[Response]
      if(! response.isCommitted)
        response.sendError(500)
      c.complete()
    }

    def onComplete(c: Continuation) {
      logRequest(request)
    }
  }
}
