package com.rallyhealth.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object ActorApp extends App {

  /**
   * Default example application.
   *
   * Sends a message to a single actor, then sends another message and waits for the response.
   */
  override def main(args: Array[String]): Unit = {
    val conf = ConfigFactory.load("application.conf")
    System.out.println(conf.getString("akka.log-config-on-start")) //should be 'on'


    val defaultSystem = ActorSystem("example-actor-system")

    // Dispatchers map a thread pool to execution that must be performed by actors
    // Actors are an abstraction over a thread. Many actors can run on a single thread - they're lighter units of concurrency!!
    // Implemented with fork-join logic
    val defaultDispatcher = defaultSystem.dispatcher

    // dispatchers implement the execution context interface
    // can be used for running futures
    implicit val ec: ExecutionContext = defaultDispatcher
    implicit val timeout: Timeout = 5 seconds

    // Configuration
    val helloWorldProps: Props = HelloWorldActor.props("Seho")
    val helloWorldActor: ActorRef = defaultSystem.actorOf(helloWorldProps, "hello-world-actor")

    // Message passing
    // Tell - we can't receive any messages because we do not have a mailbox
    helloWorldActor ! HelloWorldActor.Greet("IAmApplication")
    // Ask - we can instead wait for a response by using an ask which returns a future
    val resp: Future[HelloWorldActor.Greet] = (helloWorldActor ? HelloWorldActor.Wave).mapTo[HelloWorldActor.Greet]

    resp.map { r =>
      println(s"I am Application. Hello ${r.name}")
      defaultSystem.terminate()
    }

  }

}
