package com.nvr.songengine.player

import akka.actor.{Actor, ActorRef}
import akka.event.slf4j.Logger

import scala.collection.mutable

/**
 * Created by vinay.varma on 2/2/15.
 */
case class SubscribeReceiver(receiverActor: ActorRef)

case class UnsubscribeReceiver(receiverActor: ActorRef)

class RemoteEventListener extends Actor with EventListener {
  val logger=Logger("RemoteEventListener")
  var receivers: mutable.LinkedList[ActorRef] = new mutable.LinkedList[ActorRef]()

  override def receive: Receive = {
    case SubscribeReceiver(receiverActor: ActorRef) =>
      logger.info("received subscribe from %s".format(receiverActor.toString()))
      receivers = mutable.LinkedList(receiverActor) ++ receivers

    case UnsubscribeReceiver(receiverActor: ActorRef) =>
      logger.info("received unsubscribe from %s".format(receiverActor.toString()))
      receivers = receivers.dropWhile(x => x eq receiverActor)
    case eventMessage: EventMessage =>
      receivers.foreach(_ ! eventMessage)
  }

  override def apply(eventMessage: EventMessage): Unit = {
    receivers.foreach(_ ! eventMessage)
  }
}
