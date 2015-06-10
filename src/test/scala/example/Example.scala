package example

object Example extends App {
  import fender._
  import anodyne.Matching._
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global

  val s1 = server (
    connector(host("localhost") ~ port(9897)) ~
    connector(port(9898)) ~
    routes(
      
      path("/test1") ~ respond { 
        case GET()  => Plain ~ "Hello World\n" 
        case POST() => Plain ~ "Noted\n"
      },
      
      path("/test2") ~ react { 
        case GET() => complete( Plain ~ "Hello World Too\n") 
      }
    )
    ~ started
  )

  val s2 = server (
    connector(port(9899)) ~
    react { 
      case GET() & PathParts("test") => complete(Plain ~ "Hello Another World\n")

      case GET() & PathParts("svg")  => runFuture(Future( SVG ~  
          <svg xmlns={SVG.NS} version="1.1"><circle r="100"/></svg> 
        )
      )
      
      case GET() & PathParts("diagram") => complete(XHTML ~
        <html xmlns={XHTML.NS}>
          <head><title>Diagram</title></head>
          <body>
            <h2>Pie Chart</h2>
            <img src="svg"/>
            <form action="http://localhost:9897/test1/" method="post">
              <input type="submit"/>
            </form>
          </body>
        </html>
      )
    }
    ~ started
  )

  val instance1 = s1.run()
  val instance2 = s2.run()

  logger info "ready ready ready"
  io.StdIn.readLine()

  instance1.stop
  instance2.stop
}
