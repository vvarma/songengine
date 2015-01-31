import akka.actor.Actor

import scala.collection.mutable

/**
 * Created by vinay.varma on 1/31/15.
 */
case class RegisterListener(listener: EventListener)

class Events extends Actor {
  var listeners = new mutable.MutableList[EventListener]

  override def receive: Receive = {
    case RegisterListener(listener) =>
      listeners += listener
    case event:Event=>
      listeners.foreach(listener=>listener.apply(event))
    case EventMessage(event)=>
      listeners.foreach(listener=>listener.apply(event))
  }

}
case class EventMessage(event:Event)