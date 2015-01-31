import akka.actor.{Actor, ActorRef}
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
 * Created by vinay.varma on 1/15/15.
 */
case class Play()

case class Add(songs: Iterable[String])

case class Next()

case class Stop()

case class GetPlayList()

case class PlayList()

case class Playing(songs: Iterable[String])

case class GetPlayerState()

case class PlayerState(isPlaying: Boolean)

case class Start()

case class Pause()

case class Feed(line: String)

case class Register(path: String)

case class RegisteredSongs(songs: Array[Song])


class Player extends Actor with PlayerEventHandler {
  implicit val timeout = Timeout(5 seconds)
  val player = new VlcPlayer(this)

  override def receive: Receive = {
    case Play() =>
      player.play()
    case Add(songs: Iterable[String]) =>
      player.addToPlayList(songs.toArray)
    case Next() =>
      player.next()
    case Stop() =>
      player.stop()
    case Pause() =>
      player.pause()
    case PlayList() =>
      sender ! Playing(player.playList)
    case GetPlayerState() =>
      sender ! PlayerState(player.isPlaying)
    case Register(path) =>
      val registeredSongs = for (songDetail <- player.registerSongsFromHomePath(path)) yield new Song(songDetail)
      registeredSongs.foreach(song =>
        resolveActorRef("/user/supervisor/eventsRef")(actorRef => actorRef ! EventMessage(new RegisterSong(song)))
      )
      sender ! RegisteredSongs(registeredSongs)
    case _ =>
      println("Unknown message ")

  }

  override def onPlay(): Unit = {
    resolveActorRef("/user/supervisor/eventsRef")(actorRef => actorRef ! EventMessage(new PlayingSong))
  }

  override def onNext(songDetails: SongDetails): Unit = {
    resolveActorRef("/user/supervisor/eventsRef")(actorRef => actorRef ! EventMessage(new NextSong(new Song(songDetails))))
    resolveActorRef("/user/supervisor/engineRef")(actorRef => actorRef ! GetPlayList())
  }

  override def onStop(): Unit = {
    resolveActorRef("/user/supervisor/eventsRef")(actorRef => actorRef ! EventMessage(new StoppedSong))
  }

  def resolveActorRef(actorUrl: String)(onSuccessFn: ActorRef => Unit) {
    context.actorSelection(actorUrl).resolveOne().onComplete {
      case Success(actorRef) =>
        onSuccessFn(actorRef)
      case Failure(ex) =>
        println("error resolving player ref.")
    }
  }
}
