package com.nvr.songengine.player

import akka.actor.{ActorRef, Actor}

import scala.collection.mutable

/**
 * Created by vinay.varma on 2/2/15.
 */

class Events extends Actor {
  var listeners = new mutable.MutableList[ActorRef]

  override def receive: Receive = {
    case RegisterListener(listener) =>
      listeners += listener
    case eventMessage: EventMessage =>
      listeners.foreach(listener => listener ! eventMessage)
  }

}

case class EventMessage(event: Event)

case class RegisterListener(listener: ActorRef)