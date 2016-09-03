package example

object Example extends App {
  import fender._
  import anodyne.Matching._
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext.Implicits.global

  val s1 = server (
    connector(host("localhost") ~ port(9897)) ~
    connector(port(9898)) ~
    contexts(
      context(
        path("/test1") ~
        handlers(
          respond {
            case GET(_)  => Plain("Hello World\n")
            case POST(_) => Plain("Noted\n")
          }
        )
      ) ~

      context(
        path("/test2") ~
        handlers(
          react {
            case GET(_) => complete(Plain("Hello World Too\n"))
          }
        )
      ) ~
      defaultHandler
    )
    ~ started
  )

  val s2 = server (
    connector(port(9899)) ~
    handlers(
      react {
        case GET(Path("test")) => complete(Plain("Hello Another World\n"))

      }
    )
    ~ started
  )

  val instance1 = s1.run()
  val instance2 = s2.run()

  logger info "ready ready ready"
  io.StdIn.readLine()

  instance1.stop
  instance2.stop
}
