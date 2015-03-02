package com.nvr.songengine.recco

import com.nvr.songengine.player.Song

import scala.collection.mutable
import scala.collection.immutable.Queue


/**
 * Created by vinay.varma on 2/22/15.
 */
object State {
  val gramLevel = 2

  implicit def queue2finitequeue[A](q: Queue[A]) = new FiniteQueue[A](q)

  val songs = new mutable.MutableList[Song]
  var songQueue = Queue.empty[Song]
  val songTrie = new SongTrie(-1)
  val songRating = mutable.Map.empty[Song, Int]

  def getHighRatedSong = {
    mutable.ListMap(songRating.toList sortBy {
      _._2
    }: _*).head
  }

  def registerSong(song: Song) {
    if (!songs.contains(song)) {
      song.id = State.songs.size
      songs += song
      appendToFile(songStore, mapper.writeValueAsString(song))
    }
  }

  def updateState(optionalSong: Option[Song], rating: Int) {
    if (songQueue.size > 0) {
      songRating.get(songQueue.last) match {
        case Some(userSongRating) =>
          songRating.put(songQueue.last, userSongRating + rating)
        case None =>
          songRating.put(songQueue.last, rating)
      }
    }
    if (songQueue.size == gramLevel) {
      songTrie.upsert(songQueue, rating)
    }

    optionalSong match {
      case Some(song) =>
        songQueue = songQueue.enqueueFinite(song, gramLevel)
      case None =>
    }
  }

}

class FiniteQueue[A](q: Queue[A]) {
  def enqueueFinite[B >: A](elem: B, maxSize: Int): Queue[B] = {
    var ret = q.enqueue(elem)
    while (ret.size > maxSize) {
      ret = ret.dequeue._2
    }
    ret
  }
}

class SongTrie(val songId: Int) {
  private val nextSongMap = new mutable.HashMap[Int, SongTrie]()
  var rating = 0

  def upsert(songQueue: Queue[Song], rating: Int) {
    val revQueue = songQueue.reverse
    if (revQueue.size > 0) {
      val songId = revQueue.head.id
      nextSongMap.get(songId) match {
        case Some(hasNext) =>
          hasNext.upsert(revQueue.tail, rating)
        case None =>
          val songTrie = new SongTrie(songId)
          songTrie.upsert(revQueue.tail, rating)
          nextSongMap.put(songId, songTrie)
      }
    } else {
      this.rating += rating
    }
  }

  def getAllRelated(songIds: Seq[Int]): Map[Int,Int] = {
    var localTrie = this
    for (id <- songIds) {
      localTrie.nextSongMap.get(id) match {
        case Some(trie) =>
          localTrie = trie
        case None =>
        //          localTrie=new SongTrie(-1)
      }
    }
    val res=for (tr <-localTrie.nextSongMap.values)yield {
      (tr.songId,tr.getAggregatedRating)
    }
    res.toMap
  }

  def getAggregatedRating: Int = {
    var rating = this.rating
    nextSongMap.values.foreach(tr => rating += tr.getAggregatedRating)
    rating
  }


}
