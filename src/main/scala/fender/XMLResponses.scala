package fender

import java.util.zip.{ZipOutputStream, ZipEntry}
import scala.xml.{Elem, XML}
import java.io.{StringWriter, OutputStream, OutputStreamWriter}
import org.eclipse.jetty.server.Response



trait XMLResponses extends Responses {

  def writeXML(node: Elem, stream: OutputStream): Unit = {
    val enc = "UTF-8"
    val writer = new OutputStreamWriter(stream, enc)
    XML.write( writer, node, enc, true, null)
    writer.flush
  }

  implicit val xml: Content[Elem] = assign { (r, x) => writeXML(x, r.getOutputStream) }

  val kmz: Content[Elem] = assign {
    (r, x) =>
      r.setContentType(KMZ.mime)
      val z = new ZipOutputStream(r.getOutputStream)
      z.putNextEntry(new ZipEntry("doc.kml"))
      writeXML(x, z)
      z.finish
  }
}
