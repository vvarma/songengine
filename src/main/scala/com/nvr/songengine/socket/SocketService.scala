package com.nvr.songengine.socket

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.nvr.songengine.player.EventMessage
import com.nvr.songengine._

/**
 * Created by vinay.varma on 3/20/15.
 */
class SocketService(hostName: String, port: Int, listener: ActorRef) extends Actor with ActorLogging {
  val endpoint = new InetSocketAddress(hostName, port)

  import context.system

  IO(Tcp) ! Tcp.Bind(self, endpoint)

  override def receive: Receive = {
    case Tcp.Connected(remote, _) =>
      log.debug("Remote address {} connected", remote)
      sender ! Tcp.Register(context.actorOf(SocketConnectionHandler.props(remote, sender(), listener), "cxHandle"))
  }
}

object SocketService {
  def props(hostName: String, port: Int, listener: ActorRef) = {
    Props(new SocketService(hostName, port, listener))
  }
}

object SocketConnectionHandler {
  def props(remote: InetSocketAddress, connection: ActorRef, listener: ActorRef): Props =
    Props(new SocketConnectionHandler(remote, connection, listener))
}

class SocketConnectionHandler(remote: InetSocketAddress, connection: ActorRef, listener: ActorRef) extends Actor with ActorLogging {

  // We need to know when the connection dies without sending a `Tcp.ConnectionClosed`
  context.watch(connection)

  def receive: Receive = {
    case data: EventMessage =>
      val msg = serialize(data)
      log.debug("Writing data " + msg)
      connection ! Tcp.Write(ByteString(msg + "\n"))
    case Tcp.Received(data) =>
      val text = data.utf8String.trim
      log.debug("Received '{}' from remote address {}", text, remote)
      listener ! data
    case _: Tcp.ConnectionClosed =>
      log.debug("Stopping, because connection for remote address {} closed", remote)
      context.stop(self)
    case Terminated(`connection`) =>
      log.debug("Stopping, because connection for remote address {} died", remote)
      context.stop(self)
  }
}
