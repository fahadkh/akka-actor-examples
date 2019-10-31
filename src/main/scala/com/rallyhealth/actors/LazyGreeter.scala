package com.rallyhealth.actors

import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, Props}
import com.rallyhealth.actors.HelloWorldActor.Greet
import com.rallyhealth.actors.LazyGreeter.ConferenceCall

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class LazyGreeter(myName: String) extends Actor with ActorLogging {

  var greeter: ActorRef = _

  /** Here we are using the dispatcher as our execution context. This is dangerous if we use the default dispatcher
    * and have blocking operations that will starve the thread pool used by the entire system.
    *
    * If blocking cannot be avoided. Be sure to give this actor its own bounded thread pool
    * blocking dispatcher (see BadActorApp.scala)
    */
  implicit val ec: ExecutionContext = context.dispatcher

  // Interaction with mailbox
  def receive: PartialFunction[Any, Unit] = {
    case ConferenceCall(name) =>
      println(s"I am $myName. Calling the greeter...")

      // Run a blocking operation on a different thread (avoid if possible and see comment on line 21
      Future {
        Thread.sleep(5000)
        greeter ! Greet(s"$myName on behalf of $name")
      }

    case Greet(name) =>
      println(s"I am $myName. $name has responded")

    case _ => println("Wut")
  }

  // Lifecycle hooks
  override def preStart(): Unit = {
    // We can create other actors here too
    // Context is our own level of the actor system hierarchy
    // This actor is our child and belongs only to use, but can be killed recursively from the top level
    greeter = context.actorOf(HelloWorldActor.props("Ben"), "hello-world-actor-2")
    super.preStart()
  }

  // Override the supervision strategy for this context. Any actors created by our context (context.actorOf) will be
  // supervised by this code block
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ArithmeticException      => Resume
      case _: NullPointerException     => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception                => Escalate
    }

}

object LazyGreeter {
  // Props are a recipe for creating an instance of this actor
  // Contain immutable, serializable configuration
  def props(myName: String): Props = Props(new LazyGreeter(myName))

  case class ConferenceCall(name: String)
}


