package com.nvr.songengine.player

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import com.nvr.songengine.feedback.{Action, UserAction}
import com.nvr.songengine.player.PathConstants.{PLAYER_REF, RECCO_REF}
import com.nvr.songengine.recco.{GetReccomendation, Reccommendation}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
 * Created by vinay.varma on 2/2/15.
 */

/**
 * Created by vinay.varma on 1/14/15.
 */
class SongEngine extends Actor {
  val logger = Logging.getLogger(context.system,this)
  implicit val timeout = Timeout(5 seconds)


  override def receive: Receive = {
    case Start() =>
      logger.info("Start")
      resolveActorRef(PLAYER_REF)(actorRef => {
        actorRef ! Register(PathConstants.HOME)
      })
    case RegisteredSongs(registeredSongs) =>
      logger.info("Register")
      self ! GetPlayList()
    case GetPlayList() =>
      logger.info("GetPlayList")
      resolveActorRef(PLAYER_REF)(actorRef => actorRef ! PlayList())
    case Playing(songs) =>
      logger.info("Playing")
      val playListSize = songs.size
      if (playListSize < 10)
        resolveActorRef(RECCO_REF) {
          actorRef => (actorRef ? GetReccomendation(10 - playListSize, None)).onSuccess {
            case reco: Reccommendation =>
              resolveActorRef(PLAYER_REF)(actorRef => {
                actorRef ! Add(reco.songs)
                actorRef ! GetPlayerState()
              })
          }
        }
    case PlayerState(isPlaying) =>
      logger.info("PlayerState")
      if (!isPlaying)
        resolveActorRef(PLAYER_REF)(actorRef => {
          actorRef ! Play()
        })
    case Action(userAction, songRating, orderRating) =>
      logger.info("Action")
      userAction match {
        case UserAction.PauseAction =>
          resolveActorRef(PLAYER_REF)(actorRef => actorRef ! Pause())
        case UserAction.SkipAction =>
          resolveActorRef(PLAYER_REF)(actorRef => actorRef ! Next())
        case UserAction.StopAction =>
          resolveActorRef(PLAYER_REF)(actorRef => actorRef ! Stop())
        case _ =>
          logger.info("Empty Action!")
      }
    case _ =>
      logger.info("Unknown message in engine")

  }

  def resolveActorRef(url: String)(onSuccessFn: ActorRef => Unit) {
    context.actorSelection(url).resolveOne().onComplete {
      case Success(actorRef) =>
        onSuccessFn(actorRef)
      case Failure(ex) =>
        logger.error("error resolving" + url)
    }
  }
}

object PathConstants {
  var HOME = "/Users/vinay.varma/Music"
  val PLAYER_REF = "/user/supervisor/playerRef"
  val EVENTS_REF = "/user/supervisor/eventsRef"
  val ENGINE_REF = "/user/supervisor/engineRef"
  val RECCO_REF = "/user/supervisor/reccoRef"

}
