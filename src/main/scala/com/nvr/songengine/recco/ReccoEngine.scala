package com.nvr.songengine.recco

import java.io.FileNotFoundException

import akka.actor.Actor
import akka.event.Logging
import com.nvr.songengine.feedback.UserAction
import com.nvr.songengine.player._

import scala.collection.mutable
import scala.io.Source
import scala.util.Random
import com.nvr.songengine._

/**
 * Created by vinay.varma on 2/4/15.
 */
case class GetReccomendation(count: Int, currentSong: Option[Song])

case class Reccommendation(songs: Array[Song])

class ReccoEngine extends Actor {
  val events = new mutable.MutableList[Event]

  val logger = Logging.getLogger(context.system,this)

  override def preStart(): Unit = {
    try {
      for (line <- Source.fromFile(songStore).getLines()) {
        State.songs += mapper.readValue(line, classOf[Song])
      }
      for (line <- Source.fromFile(eventStore).getLines()) {
        handleEvent(mapper.readValue(line, classOf[Event]))
      }
    } catch {
      case e: FileNotFoundException =>
        logger.info(songStore + " doesnt exist")
    }
  }

  override def receive: Receive = {
    case getReco: GetReccomendation =>
      sender ! Reccommendation(Random.shuffle(State.songs).take(getReco.count).toArray)
    case eventMessage: EventMessage => eventMessage.event match {
      case registerSong: RegisterSong =>
        State.registerSong(registerSong.song)
      case event: Event => {
        handleEvent(event)
      }
        events += event
        appendToFile(eventStore, mapper.writeValueAsString(event))
    }
    case _ =>
      logger.info("Unknown message")
  }

  def handleEvent(event: Event): Unit = event match {
    case nextSong: NextSong =>
      State.updateState(Some(nextSong.song), 1)
    case feedbackSong: FeedbackSong =>
      feedbackSong.action.action match {
        case UserAction.SkipAction =>
          State.updateState(None, -2)
        case _ =>
          logger.info("Unhandled feedback")
      }
    case _ =>
      logger.info("Unhandled event")
  }
}

