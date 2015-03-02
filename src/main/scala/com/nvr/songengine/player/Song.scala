package com.nvr.songengine.player

/**
 * Created by vinay.varma on 1/31/15.
 */
class Song(val songDetail: SongDetails) extends Serializable {
  var id: Int = 0

  def mrl = songDetail.mrl

  override def toString = "{\"songDetail\":" + songDetail.toString + "}"


  def canEqual(other: Any): Boolean = other.isInstanceOf[Song]

  override def equals(other: Any): Boolean = other match {
    case that: Song =>
      (that canEqual this) &&
        songDetail == that.songDetail
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(songDetail)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

