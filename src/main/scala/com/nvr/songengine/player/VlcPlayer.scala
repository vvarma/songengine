package com.nvr.songengine.player

import java.io.File

import akka.actor.ActorSystem
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t
import uk.co.caprica.vlcj.component.AudioMediaPlayerComponent
import uk.co.caprica.vlcj.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.MediaPlayer
import uk.co.caprica.vlcj.player.list.{MediaListPlayer, MediaListPlayerEventAdapter}
import uk.co.caprica.vlcj.version.LibVlcVersion

import scala.collection.mutable

/**
 * Created by vinay.varma on 1/31/15.
 */
class VlcPlayer(val eventHandler: PlayerEventHandler) {
  //jest only for logging
  val system = ActorSystem("VlcPlayer", ConfigFactory.load.getConfig("akka"))
  val logger = Logging.getLogger(system, this)
  logger.info("Discovery " + new NativeDiscovery().discover())
  val version = LibVlcVersion.getVersion
  logger.info("Found version " + version.toString)
  val audioPlayer = new AudioMediaPlayerComponent {
    override def finished(mediaPlayer: MediaPlayer) {
      logger.error("Vlc Player is finished")
    }

    override def error(mediaPlayer: MediaPlayer) {
      logger.error("Vlc Player has errored out")
      System.exit(1)
    }
  }
  val mediaPlayer: MediaPlayer = audioPlayer.getMediaPlayer
  val mediaListPlayer = audioPlayer.getMediaPlayerFactory.newMediaListPlayer
  mediaPlayer.setRepeat(true)
  var transientPlayList = new mutable.MutableList[Song]

  mediaListPlayer.setMediaPlayer(mediaPlayer)


  mediaListPlayer.setMediaList(audioPlayer.getMediaPlayerFactory.newMediaList)


  mediaListPlayer.addMediaListPlayerEventListener(new MediaListPlayerEventAdapter {
    override def nextItem(mediaListPlayer: MediaListPlayer, item: libvlc_media_t, itemMrl: String) {
      eventHandler.onNext(transientPlayList.head)
      transientPlayList = transientPlayList.tail
    }
  })

  def registerSongsFromHomePath(path: String) = {
    logger.info("Registering songs from path " + path)
    val home = new File(path)
    if (!home.isDirectory) throw new RuntimeException

    val allFiles = recursiveListFiles(home)
    logger.info("Found " + allFiles.size + " files. Filtering with ends with mp3")
    val mp3Files = allFiles.filter(file => file.getName.endsWith("mp3"))
    logger.info("Found " + mp3Files.size + " mp3 files.")
    val mp3s = mp3Files.map(f => f.getAbsolutePath)
    for (mp3 <- mp3s) yield {
      SongDetails.create(audioPlayer.getMediaPlayerFactory.getMediaMeta(mp3, true), mp3)
    }
  }

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }


  def addToPlayList(songs: Array[Song]) {
    for (song <- songs) {
      mediaListPlayer.getMediaList.addMedia(song.mrl)
      transientPlayList += song
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

  def playList: Iterable[Song] = {
    transientPlayList
  }
}
