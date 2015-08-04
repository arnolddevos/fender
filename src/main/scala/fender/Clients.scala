package fender

import org.eclipse.jetty
import jetty.client.{HttpClient}

trait Clients extends Containers {
  def client = build { new HttpClient }

}
