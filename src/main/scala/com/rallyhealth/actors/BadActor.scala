package com.rallyhealth.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import com.rallyhealth.actors.BadActor.{Add, Echo, InternalAdd, Print}

import scala.concurrent.{ExecutionContext, Future}

class BadActor extends Actor with ActorLogging {

  var counter: Int = 0
  implicit val ec: ExecutionContext = context.dispatcher

  def receive: PartialFunction[Any, Unit] = {

    // BAD: Accessing state on different threads will cause race conditions. We will miss some add commands
    case Add() => futureAddOne()
    // BETTER: Will requeue side effect on main thread
    // case Add() => safelyMixingInFutures()
    // BEST: Avoid future all together
    // case Add() => addOne()

    case InternalAdd() => counter += 1

    case Print() => println(counter)

    // Send a message to the sender on a separate thread. The sender() ref may have moved on to a different message
    // and no longer points to the correct location on our other thread. The caller may never get a response or the
    // response will be sent to the dead-letters queue
    case Echo(msg) => futureEcho(msg).map(m => sender() ! m)
    // BETTER: Use pipeTo to save the sender() ref
    // case Echo(msg) => futureEcho(msg) pipeTo sender()

    case _ => println("wut")
  }

  def addOne(): Unit = {
    counter += 1
  }

  def futureAddOne(): Future[Unit] = {
    Future(counter += 1)
  }

  // Executes an async operation and then updates state by sending itself a message
  def safelyMixingInFutures(): Future[Unit] = {
    Future{
      //Async operation
    }.map(_ => self ! InternalAdd())
  }

  def futureEcho(msg: String): Future[String] = {
    Future(msg)
  }

}

object BadActor {
  def props: Props = Props[BadActor]

  case class Add()
  case class InternalAdd()
  case class Print()
  case class Echo(str: String)
}




