package com.nvr.songengine.player

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.util.Timeout
import com.nvr.songengine.player.PathConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}


/**
 * Created by vinay.varma on 1/15/15.
 */
case class Play()

case class Add(songs: Iterable[Song])

case class Next()

case class Stop()

case class GetPlayList()

case class PlayList()

case class Playing(songs: Iterable[Song])

case class GetPlayerState()

case class PlayerState(isPlaying: Boolean)

case class Start()

case class Pause()

case class Feed(line: String)

case class Register(path: String)

case class RegisteredSongs(songs: Array[Song])


class Player extends Actor with PlayerEventHandler {
  val logger = Logging.getLogger(context.system, this)
  logger.info("Instantiating player actor")
  implicit val timeout = Timeout(5 seconds)
  var player: VlcPlayer = null

  override def preStart(): Unit = {
    player =
      try {
        new VlcPlayer(this)
      } catch {
        case e: Exception =>
          logger.error(e, "Error instantiating Vlc Player.")
          throw e;
      }

  }

  override def receive: Receive = {
    case Play() =>
      player.play()
    case Add(songs) =>
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
      logger.info("Got " + registeredSongs.size + " songs registered")
      registeredSongs.foreach(song =>
        resolveActorRef(EVENTS_REF)(actorRef => actorRef ! EventMessage(new RegisterSong(song)))
      )
      sender ! RegisteredSongs(registeredSongs)
    case _ =>
      logger.error("Unknown message ")

  }

  override def onPlay(): Unit = {
    resolveActorRef(EVENTS_REF)(actorRef => actorRef ! EventMessage(new PlayingSong))
  }

  override def onNext(song: Song): Unit = {
    resolveActorRef(EVENTS_REF)(actorRef => actorRef ! EventMessage(new NextSong(song)))
    resolveActorRef(ENGINE_REF)(actorRef => actorRef ! GetPlayList())
  }

  override def onStop(): Unit = {
    resolveActorRef(EVENTS_REF)(actorRef => actorRef ! EventMessage(new StoppedSong))
  }

  def resolveActorRef(actorUrl: String)(onSuccessFn: ActorRef => Unit) {
    context.actorSelection(actorUrl).resolveOne().onComplete {
      case Success(actorRef) =>
        onSuccessFn(actorRef)
      case Failure(ex) =>
        logger.error("error resolving player ref.")
    }
  }
}
