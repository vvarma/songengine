/**
 * Created by vinay.varma on 1/31/15.
 */
class Song(val songDetail: SongDetails) {

  def mrl=songDetail.mrl
  override def toString = "{\"songDetail\":" + songDetail.toString + "}"


}
