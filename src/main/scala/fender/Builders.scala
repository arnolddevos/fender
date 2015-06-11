package fender

trait Builders {

  trait Build[+A] { self =>
    def run(): A
    def extend(cf: Config[A]) = new Build[A] {
      def run() = { val a = self.run(); cf.affect(a); a }
    }
  }

  trait Config[-A] { self =>
    def affect(a: A): Unit
    def andThen[B <: A]( cf: Config[B]) = new Config[B] {
      def affect(b: B) = {self.affect(b); cf.affect(b)}
    }
  }

  def build[A]( a: => A ) = new Build[A] {
    def run() = a
  }

  def config[A](k: A => Unit) = new Config[A] {
    def affect(a: A) = k(a)
  }

  def assign[A, B](k: (A, B) => Unit): B => Config[A] = 
    b => config(a => k(a, b))

  def inject[A, B](k: (A, B) => Unit): Build[B] => Config[A] =
    blb => config(a => k(a, blb.run()))

  def map[A, B](f: A => B): Build[A] => Build[B] = 
    bla => build(f(bla.run()))

  def contra[A, B](f: A => B): Config[B] => Config[A] =
    cfb => config(a => cfb.affect(f(a)))

  def compose0[A, B](inj: Build[B] => Config[A], blb: Build[B] ): Config[B] => Config[A] = 
    cfb => inj(blb.extend(cfb))

  def compose1[A, B](f: Build[B] => Config[A], g: Build[A] => Build[B]): Config[B] => Config[A] =
    cfb => config { a => f(g(build(a)).extend(cfb)).affect(a) }

  def pass[A] = config[A](_ => ())

  def coreturn[A]( bla: Build[A]): A = bla.run()

  def cobind[A, B](f: Build[A] => B): Build[A] => Build[B] =
    bla => build(f(bla))

  def cokleisli[A](cfa: Config[A]): Build[A] => A =
    bla => bla.extend(cfa).run()

}
