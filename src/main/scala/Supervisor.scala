import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt

/**
 * Created by vinay.varma on 1/24/15.
 */

class Supervisor extends Actor {
  val playerRef = context.actorOf(Props[Player], "playerRef")
  val engineRef = context.actorOf(Props[SongEngine], "engineRef")
  val feedBackRef = context.actorOf(Props[Feedback], "feedBackRef")
  val eventsRef = context.actorOf(Props[Events], "eventsRef")

  new Thread() {
    override def run(): Unit = {
      while (true) {
        feedBackRef ! Feed(readLine())
      }
    }
  }.start()

  override def receive: Receive = {
    case Start() =>
      eventsRef ! RegisterListener(new SimpleEventListener)
      engineRef ! Start()
    case _ =>
      println("unknown msg")
  }
}

object Supervisor {
  implicit val timeout = Timeout(5 seconds)

  val system = ActorSystem("SongEngine")

  def main(args: Array[String]) {
    val supervisor = system.actorOf(Props[Supervisor], name = "supervisor")
    supervisor ! Start()
    Thread.currentThread().join()
  }
}

