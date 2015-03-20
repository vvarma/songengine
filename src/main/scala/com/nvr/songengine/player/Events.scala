package com.nvr.songengine.player

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.util.ByteString

import scala.collection.mutable

/**
 * Created by vinay.varma on 2/2/15.
 */

class Events extends Actor with ActorLogging {
  var listeners = new mutable.MutableList[ActorRef]

  override def receive: Receive = {
    case RegisterListener(listener) =>
      listeners += listener
      log.debug("Registered listener" + listeners.size + " :" + listener.path)
    case data: ByteString =>
      val line = data.utf8String
      line.stripLineEnd match {
        case "register" =>
          listeners += sender()
        case _ =>
      }
      log.debug("Registered listener" + listeners.size + " :" + sender().path)
    case eventMessage: EventMessage =>
      listeners.foreach(listener => listener ! eventMessage)
  }

}

case class EventMessage(event: Event)

case class RegisterListener(listener: ActorRef)