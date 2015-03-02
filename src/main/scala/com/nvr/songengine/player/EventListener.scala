package com.nvr.songengine.player

import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.{JsonIgnore, JsonSubTypes, JsonTypeInfo}

/**
 * Created by vinay.varma on 1/31/15.
 */
trait EventListener {

  def apply(eventMessage: EventMessage)

}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "name")
@JsonSubTypes(Array(
  new Type(value = classOf[RegisterSong], name = "REGISTER"),
  new Type(value = classOf[NextSong], name = "NEXT"),
  new Type(value = classOf[PlayingSong], name = "PLAYING"),
  new Type(value = classOf[StoppedSong], name = "STOPPED"),
  new Type(value = classOf[FeedbackSong], name = "FEED")
))
abstract class Event extends Serializable {
  @JsonIgnore
  def getName: String
}

class RegisterSong(val song: Song) extends Event {
  override def getName: String = "REGISTER"
}

class NextSong(val song: Song) extends Event {
  override def getName: String = "NEXT"
}

class PlayingSong extends Event {
  override def getName: String = "PLAYING"
}

class StoppedSong extends Event {
  override def getName: String = "STOPPED"
}

class FeedbackSong(val action: Action) extends Event {
  override def getName: String = "FEED"
}

class SimpleEventListener extends EventListener {
  override def apply(eventMessage: EventMessage): Unit = {
    println(eventMessage)
  }
}
