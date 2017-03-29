package fender

import org.eclipse.jetty
import jetty.client._
import jetty.client.api._
import jetty.client.util._
import flowlib._
import Process._

trait Clients { this: Builders with Containers =>
  def client = build { new HttpClient  }
  def followRedirects = assign[HttpClient, Boolean] {_ setFollowRedirects _}

  def sendGET(url: String, limit: Int)(implicit pool: HttpClient): Process[String] = {

    def fetch: Process[Process[String]] = waitFor {
      k =>

        val l = new BufferingResponseListener(limit) {
          override def onSuccess(r: Response): Unit = {
            if(r.getStatus == 200)
              k(stop(getContentAsString()))
            else
              k(fail("taglist not available: " + r.getReason))
          }
          override def onFailure(r: Response, e: Throwable): Unit = {
            k(fail("taglist not available", e))
          }
          override def onComplete(r: Result): Unit = {}
        }

        pool.newRequest(url).send(l)

    }

    fetch >>= identity
  }
}
