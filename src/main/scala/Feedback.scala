import akka.actor.{Actor, ActorRef}
import UserAction.UserAction
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

/**
 * Created by vinay.varma on 1/25/15.
 */
class Feedback extends Actor {
  implicit val timeout = Timeout(5 seconds)

  override def receive: Receive = {
    case Feed(line) =>
      val action = Action.createAction(line)
      resolveActorRef("/user/supervisor/engineRef")(actorRef => actorRef ! action)
      resolveActorRef("/user/supervisor/eventsRef")(actorRef => actorRef ! EventMessage(new FeedbackSong(action)))
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

object UserAction extends Enumeration {
  type UserAction = Value
  val SkipAction, PauseAction, StopAction, EmptyAction = Value
}

case class Action(action: UserAction = UserAction.EmptyAction, songRating: Int = 0, orderRating: Int = 0)


object Action {
  def createAction(line: String): Action = {
    line.head match {
      case 's' =>
        Action(UserAction.StopAction)
      case 'n' =>
        Action(UserAction.SkipAction)
      case 'p' =>
        Action(UserAction.PauseAction)
      case _ =>
        Action()
    }

  }
}