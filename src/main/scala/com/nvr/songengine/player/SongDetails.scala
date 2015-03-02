package com.nvr.songengine.player

import uk.co.caprica.vlcj.player.MediaMeta

/**
 * Created by vinay.varma on 2/1/15.
 */
object SongDetails{
  def create(mediaMeta:MediaMeta,mrl:String):SongDetails=new SongDetails(mediaMeta.getArtist, mediaMeta.getGenre, mediaMeta.getLanguage, mediaMeta.getTitle, mrl)
}
class SongDetails(val artist: String, val genre: String, val language: String, val title: String, val mrl: String) extends Serializable{


  def canEqual(other: Any): Boolean = other.isInstanceOf[SongDetails]

  override def equals(other: Any): Boolean = other match {
    case that: SongDetails =>
      (that canEqual this) &&
        mrl == that.mrl
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(mrl)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

