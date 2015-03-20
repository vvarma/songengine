package com.nvr.songengine.feedback

import java.net.InetSocketAddress

import akka.actor.{Props, Actor, ActorRef}
import akka.event.Logging
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.nvr.songengine.player.Event
import com.nvr.songengine._

/**
 * Created by vinay.varma on 3/20/15.
 */
class SocketService extends Actor {
  val logger = Logging.getLogger(context.system, this)
  val socketServer = context.actorOf(Props.apply(classOf[SocketServer], self))

  override def receive: Receive = {
    case event: Event =>
      socketServer ! ByteString.apply(mapper.writeValueAsString(event))
    case data: ByteString =>
      logger.debug("Received" + data.utf8String)
  }
}

class SocketServer(val listener: ActorRef) extends Actor {

  import akka.io.Tcp._
  import context.system

  val logger = Logging.getLogger(context.system, this)
  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 3429))

  var connection: ActorRef = null

  override def receive: Actor.Receive = {
    case b@Bound(localAddress) =>
      logger.debug("Socket Server bound  to " + localAddress.toString)

    case CommandFailed(_: Bind) =>
      logger.error("Command failed to bind.")
      context stop self

    case c@Connected(remote, local) =>
      val handler = context.parent
      connection = sender()
      connection ! Register(handler)
      context become {
        case data: ByteString =>
          connection ! Write(data)
        case Received(data) =>
          listener ! data
        case "close" =>
          connection ! Close
        case _: ConnectionClosed =>
          context stop self
      }
    case _ =>
      logger.debug("Socket Server not connected.")
  }
}
