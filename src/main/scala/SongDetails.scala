import uk.co.caprica.vlcj.player.MediaMeta

/**
 * Created by vinay.varma on 2/1/15.
 */
class SongDetails(val artist: String, val genre: String, val language: String, val title: String, val mrl: String) {

  def this(mediaMeta: MediaMeta, mrl: String) {
    this(mediaMeta.getArtist, mediaMeta.getGenre, mediaMeta.getLanguage, mediaMeta.getTitle, mrl)
  }
}

