package com.rallyhealth.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.util.Timeout
import com.rallyhealth.actors.LazyGreeter.ConferenceCall
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext

object ActorSystemApp extends App {

  /**
   * Similar to ActorApp, but the LazyGreeter Actor creates its own Greeter within it's own context. It then forwards
   * requests for a conference call to it's own greeter (Ben) instead of our greeter (Seho)
   *
   */
  override def main(args: Array[String]): Unit = {
    val defaultSystem = ActorSystem("example-actor-system")

    // Dispatchers map a thread pool to execution that must be performed by actors
    // Implemented with fork-join logic
    val defaultDispatcher = defaultSystem.dispatcher

    // dispatchers implement the execution context interface
    // can be used for running futures
    implicit val ec: ExecutionContext = defaultDispatcher
    implicit val timeout: Timeout = 5 seconds

    // Configuration
    val helloWorldProps: Props = HelloWorldActor.props("Seho")
    val lazyGreeterProps: Props = LazyGreeter.props("Fahad")

    // Create our own HelloWorldActor that does not get used
    val helloWorldActor: ActorRef = defaultSystem.actorOf(helloWorldProps, "hello-world-actor-1")

    // Instead we will be using the greeter created by LazyGreeter (actor)
    val lazyGreeterActor: ActorRef = defaultSystem.actorOf(lazyGreeterProps, "lazy-greeter")

    lazyGreeterActor ! ConferenceCall("Taylor")

    //terminate this => defaultSystem.terminate()
  }

}
