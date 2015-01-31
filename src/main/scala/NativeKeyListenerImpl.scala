import akka.actor.ActorRef
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.{NativeKeyEvent, NativeKeyListener}

/**
 * Created by vinay.varma on 1/22/15.
 */
class NativeKeyListenerImpl(val actorRef:ActorRef) extends NativeKeyListener {
  GlobalScreen.registerNativeHook()

  override def nativeKeyPressed(p1: NativeKeyEvent): Unit = ???

  override def nativeKeyReleased(p1: NativeKeyEvent): Unit = ???

  override def nativeKeyTyped(p1: NativeKeyEvent): Unit = {

    p1.getKeyCode match {
      case NativeKeyEvent.VC_MEDIA_NEXT=>
        println("got next")
    }
  }

}

//object NativeKeyListenerImpl {
//  def main(args: Array[String]) {
//    new NativeKeyListenerImpl(null)
//    Thread.currentThread().join()
//  }
//}