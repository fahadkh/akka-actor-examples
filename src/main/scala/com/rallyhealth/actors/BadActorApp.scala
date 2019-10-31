package com.rallyhealth.actors

import akka.actor.ActorSystem
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object BadActorApp extends App {

  override def main(args: Array[String]): Unit = {
    val system = ActorSystem("example-actor-system")

    implicit val ec: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout = 5 seconds

    val badActor = system.actorOf(BadActor.props)

    // Send the add message 1000 times
    (0 to 999).foreach(_ => badActor ! BadActor.Add())
    // badActor _should_ respond with 1000 if there are no race conditions
    Thread.sleep(3000)
    badActor ! BadActor.Print()

    // Ask for an Echo response for numbers 0 to 20 and print when complete
    // We should print every number from 0 to 20.
    // If it's not working, you'll see skipped numbers and logs that indicate messages are being sent back to the dead letter queue
    (0 to 20).map { number =>
      val resp = (badActor ? BadActor.Echo(number.toString)).mapTo[String]
      resp.map(println)
    }

    // Here we give an actor with blocking operations its own dispatcher with a fixed thread pool so that it doesn't
    // starve our main thread pool/dispatcher of threads. (see comment on line 21 of LazyGreeter.scala)

    // We will starve ourselves of thread with this
    val badLazyActor = system.actorOf(LazyGreeter.props("Fahad"))

    // Threads will be consumed, but controlled (look at the blocking-io-dispatcher config in src/main/resources/application.conf)
    val controlledLazyActor = system.actorOf(LazyGreeter.props("Fahad").withDispatcher("my-dispatcher"))

    //system.terminate()
  }

}
