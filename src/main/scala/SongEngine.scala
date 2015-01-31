import java.io.File

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Random, Success}


/**
 * Created by vinay.varma on 1/14/15.
 */
class SongEngine extends Actor {
  implicit val timeout = Timeout(5 seconds)
  val home = new File(PathConstants.HOME)
  if (!home.isDirectory) throw new RuntimeException

  val mp3Files = recursiveListFiles(home).filter(file => file.getName.contains("mp3"))
  val mp3s = mp3Files.map(f => f.getAbsolutePath)


  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  var songs = new Array[Song](0)

  override def receive: Receive = {
    case Start() =>
      resolvePlayerRef(actorRef => {
        actorRef ! Register(PathConstants.HOME)
      })
    case RegisteredSongs(registeredSongs) =>
      this.songs = registeredSongs
      self ! GetPlayList()
    case GetPlayList() =>
      resolvePlayerRef(actorRef => {
        actorRef ! PlayList()
      })
    case Playing(songs: Iterable[String]) =>
      val playListSize = songs.size
      if (playListSize < 10)
        resolvePlayerRef(actorRef => {
          actorRef ! Add(Random.shuffle(mp3s.toList).take(10 - playListSize))
          actorRef ! GetPlayerState()
        })
    case PlayerState(isPlaying) =>
      if (!isPlaying)
        resolvePlayerRef(actorRef => {
          actorRef ! Play()
        })
    case Action(userAction, songRating, orderRating) =>
      userAction match {
        case UserAction.PauseAction =>
          resolvePlayerRef(actorRef => actorRef ! Pause())
        case UserAction.SkipAction =>
          resolvePlayerRef(actorRef => actorRef ! Next())
        case UserAction.StopAction =>
          resolvePlayerRef(actorRef => actorRef ! Stop())
        case _ =>
          println("Empty Action!")
      }
    case _ =>
      println("Unknown message in engine")

  }

  def resolvePlayerRef(onSuccessFn: ActorRef => Unit) {
    context.actorSelection("/user/supervisor/playerRef").resolveOne().onComplete {
      case Success(actorRef) =>
        onSuccessFn(actorRef)
      case Failure(ex) =>
        println("error resolving player ref.")
    }
  }
}

object SongEngine {
  def main(args: Array[String]) {
    val system = ActorSystem("SongEngine")
    val songEngineActor = system.actorOf(Props[SongEngine], name = "songEngine")
    system.scheduler.schedule(0 milliseconds, 1 minute, songEngineActor, "RUN")

  }
}

object PathConstants {
  val HOME = "/Users/vinay.varma/Music"
}
