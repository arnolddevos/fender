package fender

import org.eclipse._
import jetty.server.Server
import jetty.jmx.MBeanContainer
import java.lang.management.ManagementFactory

trait Servers extends Builders {

  val server = build { new Server }

  val withJMX = config[Server] {
    target =>
      val mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer)
      target.addBean(mbContainer)
  }

  val started = config[Server] { _.start }
}
