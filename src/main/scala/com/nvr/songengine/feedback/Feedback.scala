package com.nvr.songengine.feedback

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.util.{ByteString, Timeout}
import com.nvr.songengine.feedback.UserAction.UserAction
import com.nvr.songengine.player.PathConstants._
import com.nvr.songengine.player._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
 * Created by vinay.varma on 1/25/15.
 */
class Feedback extends Actor {
  implicit val timeout = Timeout(5 seconds)
  val logger = Logging.getLogger(context.system, this)

  override def receive: Receive = {
    case Feed(line) =>
      val action = Action.createAction(line)
      resolveActorRef(ENGINE_REF)(actorRef => actorRef ! action)
      resolveActorRef(EVENTS_REF)(actorRef => actorRef ! EventMessage(new FeedbackSong(action)))
    case data: ByteString =>
      val line = data.utf8String
      val action = Action.createAction(line.stripLineEnd)
      val senderRef = sender()
      action.action match {
        case UserAction.RegisterAction =>
          resolveActorRef(EVENTS_REF)(actorRef => actorRef ! RegisterListener(senderRef))
        case _ =>
          resolveActorRef(ENGINE_REF)(actorRef => actorRef ! action)
          resolveActorRef(EVENTS_REF)(actorRef => actorRef ! EventMessage(new FeedbackSong(action)))
      }

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

object UserAction extends Enumeration {
  type UserAction = Value
  val SkipAction, PauseAction, StopAction, RegisterAction, EmptyAction = Value
}

case class Action(action: UserAction = UserAction.EmptyAction, songRating: Int = 0, orderRating: Int = 0)


object Action {
  def createAction(line: String): Action = {
    line match {
      case "stop" =>
        Action(UserAction.StopAction)
      case "next" =>
        Action(UserAction.SkipAction)
      case "pause" =>
        Action(UserAction.PauseAction)
      case "register" =>
        Action(UserAction.RegisterAction)
      case _ =>
        Action()
    }

  }
}
