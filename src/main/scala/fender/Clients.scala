package fender

import org.eclipse.jetty
import jetty.client.{HttpClient}

trait Clients extends Containers {
  def client = build { new HttpClient  }
  def followRedirects = assign[HttpClient, Boolean] {_ setFollowRedirects _}
}
