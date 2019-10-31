package com.rallyhealth.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.rallyhealth.actors.HelloWorldActor.{Die, Greet, Wave}

class HelloWorldActor(myName: String) extends Actor with ActorLogging {

  // State, for example a cached response
  var greetedSinceCrash: Int = _

  // Interaction with mailbox, this function is run in a loop for each message
  def receive: PartialFunction[Any, Unit] = {
    case Greet(name)  => greetedSinceCrash += 1; println(s"I am $myName. Hello $name") // Executed on main thread, in the context of this specific message
    case Die          => println(s"We greeted $greetedSinceCrash others"); context.stop(self) // Actors are addressed by their ref, self is a special ref
    case Wave         => sender() ! Greet(myName) // Sender is a special ref to the sender of the message
    case _            => println("Wut")
  }

  // Lifecycle hooks
  override def preStart(): Unit = {
    // After the actor starts (before processing messages), we can initialize state here
    greetedSinceCrash = 0
    super.preStart()
  }

  // after restarts
  override def postRestart(reason: Throwable): Unit = {
    // We can also reset the state of the actor based on the reason for crashing
    greetedSinceCrash = 0
    super.postRestart(reason)
  }

  // The default implementation of preRestart() stops all the children
  // of the actor. To opt-out from stopping the children, we
  // have to override preRestart()
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    // Here we have the message that crashed us, it will be lost beyond this point
    // Keep the call to postStop(), but no stopping of children
    // postStop()
    super.preRestart(reason, message)
  }

}

object HelloWorldActor {
  // Props are a recipe for creating an instance of this actor
  // Contain immutable, serializable configuration
  def props(myName: String): Props = Props(new HelloWorldActor(myName))

  case class Greet(name: String)
  case object Wave
  case object Die
}
