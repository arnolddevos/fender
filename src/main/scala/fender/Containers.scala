package fender

import org.eclipse.jetty.{util,jmx}
import util.ssl.SslContextFactory
import util.component.ContainerLifeCycle
import jmx.MBeanContainer
import java.lang.management.ManagementFactory

trait Containers { this: Builders =>

  val started = config[ContainerLifeCycle] { _.start }

  val withJMX = config[ContainerLifeCycle] {
    target =>
      val mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer)
      target.addBean(mbContainer)
  }


}
