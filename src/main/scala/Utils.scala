package utils

import java.util.Base64

object Utils {
  val decoder = Base64.getMimeDecoder

  def decodeFileBase64(fileContent: String): String = {
    val bytes = decoder.decode(fileContent)
    new String(bytes)
  }
}
