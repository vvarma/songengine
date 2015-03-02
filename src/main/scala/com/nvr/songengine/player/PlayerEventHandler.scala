package com.nvr.songengine.player

/**
 * Created by vinay.varma on 2/1/15.
 */
trait PlayerEventHandler {
  def onPlay()

  def onStop()

  def onNext(song: Song)
}
