// Copyright 2015 Background Signal Pty Ltd

package fender

trait ContentTypes {

  class ContentType(val mime: String) 

  trait Namespace { val NS: String }

  object XHTML extends ContentType("application/xhtml+xml") with Namespace {
    val NS = "http://www.w3.org/1999/xhtml"
  }

  object SVG extends ContentType("image/svg+xml") with Namespace {
    val NS = "http://www.w3.org/2000/svg"
  }
  
  object KML extends ContentType("application/vnd.google-earth.kml+xml") with Namespace {
    val NS = "http://www.opengis.net/kml/2.2"
  }
  
  object KMZ extends ContentType("application/vnd.google-earth.kmz")
  
  object JSON extends ContentType("application/json")
  
  object Javascript extends ContentType("application/javascript")
  
  object CSV extends ContentType("text/csv")
  
  object Plain extends ContentType("text/plain")
}
