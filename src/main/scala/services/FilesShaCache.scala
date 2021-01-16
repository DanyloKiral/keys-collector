package services

import scala.collection.mutable

object FilesShaCache {
  private val processedFilesSha: mutable.HashSet[String] = mutable.HashSet.empty[String]

  def isUnknownFile(sha: String): Boolean = {
    val wasProcessed: Boolean =  processedFilesSha(sha)

    if (wasProcessed) {
      println(s"Prevented duplicate. Sha = $sha")
    } else {
      println("New File!!!")
    }

    !wasProcessed
  }

  def addProcessed(sha: String): Unit = {
    processedFilesSha.add(sha)
  }

  def clearCache(): Unit = {
    processedFilesSha.clear()
  }
}
