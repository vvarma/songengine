package com.nvr.songengine.player

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.event.slf4j.Logger
import akka.util.Timeout
import com.nvr.songengine.feedback.Feedback
import com.nvr.songengine.recco.ReccoEngine
import com.nvr.songengine.socket.SocketService

import scala.concurrent.duration.DurationInt

/**
 * Created by vinay.varma on 1/24/15.
 */

class Supervisor extends Actor {
  val logger = Logging.getLogger(context.system, this)
  val playerRef = context.actorOf(Props[Player], "playerRef")
  val engineRef = context.actorOf(Props[SongEngine], "engineRef")
  val feedBackRef = context.actorOf(Props[Feedback], "feedBackRef")
  val eventsRef = context.actorOf(Props[Events], "eventsRef")
  val reccoRef = context.actorOf(Props[ReccoEngine], "reccoRef")
  val socketService = context.actorOf(SocketService.props("192.168.1.9", 11111, feedBackRef), "socketService")


  new Thread() {
    override def run(): Unit = {
      while (true) {
        feedBackRef ! Feed(readLine())
      }
    }
  }.start()


  override def receive: Receive = {
    case Start() =>
      Thread.sleep(10000L)
      eventsRef ! RegisterListener(reccoRef)
      eventsRef ! RegisterListener(socketService)
      engineRef ! Start()
    case _ =>
      logger.error("unknown msg")
  }
}

object Supervisor {
  implicit val timeout = Timeout(5 seconds)
  val logger = Logger("org.apache.spark.SupervisorMain")
  val actorSystem = ActorSystem.create("test")
  val supervisor = actorSystem.actorOf(Props(classOf[Supervisor]), "supervisor")

  def main(args: Array[String]) {
    if (args.length > 0) {
      PathConstants.HOME = args(0)
    }

    logger.info("Feeder started as:" + supervisor)
    supervisor ! Start()
    logger.info("started supervisor")
    actorSystem.awaitTermination()
    logger.debug("Terminating actor system")
  }
}


