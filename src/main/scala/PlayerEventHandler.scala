/**
 * Created by vinay.varma on 2/1/15.
 */
trait PlayerEventHandler {
  def onPlay()

  def onStop()

  def onNext(mediaMeta: SongDetails)
}
