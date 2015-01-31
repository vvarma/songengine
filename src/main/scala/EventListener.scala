/**
 * Created by vinay.varma on 1/31/15.
 */
trait EventListener {

  def apply(event: Event)

}

abstract class Event

class RegisterSong(val song: Song) extends Event

class NextSong(val song: Song) extends Event

class PlayingSong extends Event

class StoppedSong extends Event

class FeedbackSong(val action: Action) extends Event

class SimpleEventListener extends EventListener {
  override def apply(event: Event): Unit = {
    println(event)
  }
}
