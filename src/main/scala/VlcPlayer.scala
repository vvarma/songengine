import java.io.File

import com.sun.jna.{Native, NativeLibrary}
import uk.co.caprica.vlcj.binding.LibVlc
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent
import uk.co.caprica.vlcj.player.MediaPlayer
import uk.co.caprica.vlcj.player.list.{MediaListPlayer, MediaListPlayerEventAdapter}
import uk.co.caprica.vlcj.runtime.RuntimeUtil

import scala.collection.mutable

/**
 * Created by vinay.varma on 1/31/15.
 */
class VlcPlayer(val eventHandler: PlayerEventHandler) {
  {
    NativeLibrary.addSearchPath(
      RuntimeUtil.getLibVlcLibraryName, "/Applications/VLC.app/Contents/MacOS/lib"
    )
    Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName, classOf[LibVlc])
  }
  val audioPlayer = new AudioMediaPlayerComponent {
    override def finished(mediaPlayer: MediaPlayer) {
      println("Finished")
      System.exit(0)
    }

    override def error(mediaPlayer: MediaPlayer) {
      println("Failed to play media")
      System.exit(1)
    }
  }
  val mediaPlayer: MediaPlayer = audioPlayer.getMediaPlayer
  val mediaListPlayer = audioPlayer.getMediaPlayerFactory.newMediaListPlayer
  var transientPlayList=new mutable.MutableList[String]

  mediaListPlayer.setMediaPlayer(mediaPlayer)


  mediaListPlayer.setMediaList(audioPlayer.getMediaPlayerFactory.newMediaList)


  mediaListPlayer.addMediaListPlayerEventListener(new MediaListPlayerEventAdapter {
    override def nextItem(mediaListPlayer: MediaListPlayer, item: libvlc_media_t, itemMrl: String) {
      transientPlayList=transientPlayList.tail
      eventHandler.onNext(new SongDetails(audioPlayer.getMediaPlayerFactory.getMediaMeta(itemMrl, true), itemMrl))
    }
  })

  def registerSongsFromHomePath(path: String) = {
    val home = new File(path)
    if (!home.isDirectory) throw new RuntimeException

    val mp3Files = recursiveListFiles(home).filter(file => file.getName.endsWith("mp3"))
    val mp3s = mp3Files.map(f => f.getAbsolutePath)
    for (mp3 <- mp3s) yield {
      new SongDetails(audioPlayer.getMediaPlayerFactory.getMediaMeta(mp3, true), mp3)
    }
  }

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }


  def addToPlayList(mrls: Array[String]) {
    for (mrl <- mrls) {
      mediaListPlayer.getMediaList.addMedia(mrl)
      transientPlayList+=mrl
    }
  }

  def play() {
    mediaListPlayer.play()
    eventHandler.onPlay()
  }

  def stop() {
    mediaListPlayer.stop()
    eventHandler.onStop()
  }

  def next() {
    mediaListPlayer.playNext()
  }

  def pause() {
    mediaListPlayer.pause()
  }

  def isPlaying: Boolean = {
    mediaListPlayer.isPlaying
  }

  def playList: Iterable[String] = {
    transientPlayList
  }
}
