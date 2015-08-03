// Copyright 2015 Background Signal Pty Ltd

package fender

import org.eclipse.jetty
import jetty.server.{Server, SslConnectionFactory, ConnectionFactory, ServerConnector}
import jetty.util.ssl.SslContextFactory
import jetty.http.HttpVersion
import java.net.URL

trait Connectors extends Builders {

  private def createConnector = map[Server, ServerConnector](new ServerConnector(_))

  private val addConnector = inject[Server, ServerConnector](_ addConnector _)

  val connector = compose1(addConnector, createConnector)

  val port = assign[ServerConnector, Int] { _ setPort _ }

  val host = assign[ServerConnector, String] { _ setHost _ }

  implicit val addConnectionFactory = inject[ServerConnector, ConnectionFactory] { _ addConnectionFactory _ }

  def ssl(keystore: String, pass: String) = build[ConnectionFactory] {
    val cf = new SslContextFactory
    cf.setKeyStorePath(keystore)
    cf.setKeyStorePassword(pass)
    new SslConnectionFactory(cf, HttpVersion.HTTP_1_1.asString())
  }
}
