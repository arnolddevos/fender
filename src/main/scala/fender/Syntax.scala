package fender

trait Syntax { this: Builders =>

  implicit class BuildOp[A](self: Build[A]) {
    def apply[X](x: X)(implicit ev: Magnet[X, A]): Build[A] = self.extend(ev.use(x))
  }

  implicit class ConfigOp[Y, A](y: Y)(implicit ev1: Magnet[Y, A]) {
    def ~[X](x: X)(implicit ev2: Magnet[X, A]): Config[A] = ev1.use(y) andThen ev2.use(x)
  }

  trait Magnet[X, A] {
    def use(x: X): Config[A]
  }

  implicit def identMagnet[A] =
    new Magnet[Config[A], A] { def use(cf: Config[A]) = cf }

  implicit def injectMagnet[A, B](implicit f: Build[B] => Config[A]) =
    new Magnet[Build[B], A] { def use( b: Build[B]) = f(b) }

  implicit def assignMagnet[A, B](implicit f: B => Config[A]) =
    new Magnet[B, A] { def use( b: B) = f(b) }

}
