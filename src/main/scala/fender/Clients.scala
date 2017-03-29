package fender

import org.eclipse.jetty
import jetty.client.{HttpClient}

trait Clients { this: Builders with Containers =>
  def client = build { new HttpClient  }
  def followRedirects = assign[HttpClient, Boolean] {_ setFollowRedirects _}
}
