package fender

import org.eclipse._
import jetty.server.{Handler, Request, Response}
import jetty.server.handler.AbstractHandler
import jetty.continuation.{Continuation, ContinuationSupport, ContinuationListener}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import flowlib._
import Process._
import Producers._

import scala.util.{Try, Success, Failure}
import scala.util.control.NonFatal
import scala.concurrent.{Future, ExecutionContext}

trait Reactions { this: Builders with Responses with Handlers with Logging =>

  type Reaction[T] = PartialFunction[Request, T]

  def respond(reaction: Reaction[Content]): Build[FenderHandler] = build[FenderHandler] {
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

  type DeferredContent = Config[Continuation]

  def react(reaction: Reaction[DeferredContent]): Build[FenderHandler] = build[FenderHandler] {
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

  def reactProcess(reaction: Reaction[Process[Content]])(implicit site: Site) =
    react(reaction andThen runProcess)

  def reactFuture(reaction: Reaction[Future[Content]])(implicit exec: ExecutionContext) =
    react(reaction andThen runFuture)

  def reactEducible[G](reaction: Reaction[G])(implicit e: Educible[G, Content], site: Site) =
    react(reaction andThen runEduction[G])

  def runProcess(response: Process[Content])(implicit site: Site): DeferredContent = {
    config {
      c =>
        def p = response.map(complete(_).affect(c))
        site.run("http response" !: (p recoverWith recovery(c)))
    }
  }

  def runFuture(fr: Future[Content])(implicit ex: ExecutionContext): DeferredContent = {
    config {
      d =>
        fr onComplete {
          case Success(cfr) => complete(cfr).affect(d)
          case Failure(e)   => complete(error(e)).affect(d)
        }
    }
  }

  def runEduction[G](g: G)(implicit e: Educible[G, Content], site: Site): DeferredContent = {
    config { c =>
      def a = reduce(g, responseReducer(c))
      site.run("http response" !: (a recoverWith recovery(c)))
    }
  }

  def responseReducer(c: Continuation): Reducer[Content, Unit] = {
    new Reducer[Content, Unit] {
      type State = Continuation
      def init = stop(c)
      def apply(c: Continuation, r: Content) = process {
        r.affect(c.getServletResponse.asInstanceOf[Response])
        stop(c)
      }
      def isReduced(c: Continuation) = c.isExpired
      def complete(c: Continuation) = stop(c.complete)
    }
  }

  val complete: Content => DeferredContent = {
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

  def recovery(c: Continuation): Recovery = {
    (_, e) => process {
      try {
        val r = c.getServletResponse.asInstanceOf[Response]
        if(! r.isCommitted) status(400).affect(r)
        c.complete
        stop("recovered")
      }
      catch {
        case NonFatal(e) => stop("recovery failed with " + e.toString)
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
