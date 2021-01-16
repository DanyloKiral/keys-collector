package stages

import akka.NotUsed
import akka.stream.scaladsl.Flow
import constants.Constants
import dto.{ExposedKeyData, FileWithKeyData}

import scala.util.matching.Regex

object KeySearchFlow {
  def apply(): Flow[FileWithKeyData, ExposedKeyData, NotUsed] = {
    Flow[FileWithKeyData]
      .map(d => (d, searchForSecret(d)))
      .filter(_._2.size > 0)
      .mapConcat(d => d._2.zipAll(List[FileWithKeyData](), null, d._1))
      .filter(d => extractKey(d._1).size > Constants.MinKeyLength)
      .filter(d => isKeyContainNoNoWords(d._1))
      .map(d => createExposedKeyObject(d))
  }

  def createExposedKeyObject(data: (Regex.Match, FileWithKeyData)): ExposedKeyData = {
    ExposedKeyData(
      data._2.name,
      extractKey(data._1),
      searchServiceName(data),
      data._2.html_url,
      data._2.path,
      getFileExtension(data._2.name),
      data._2.sha,
      data._2.repo.full_name,
      data._2.repo.html_url,
      "created")
  }

  def searchServiceName(data: (Regex.Match, FileWithKeyData)): Option[String] = {
    val foundServices = Constants.ServiceDetectionRegex.findAllIn(data._2.content).matchData.toList

    if (foundServices.isEmpty) {
      return None
    }

    val keyLocation = matchLocation(data._1)
    val closestServiceMatch = foundServices.minBy(s => Math.abs(keyLocation - matchLocation(s)))

    Option(closestServiceMatch.group(0))
  }

  def searchForSecret(data: FileWithKeyData): List[Regex.Match] = {
    Constants.SecretSearchRegex.findAllIn(data.content).matchData.toList
  }

  def extractKey(keyMatch: Regex.Match): String = {
    keyMatch.subgroups(Constants.SearchSecretGroupIndex)
  }

  def isKeyContainNoNoWords(keyMatch: Regex.Match): Boolean = {
    Constants.KeyNoNoWordsRegex.findFirstIn(extractKey(keyMatch)).isEmpty
  }

  def getFileExtension(fileName: String): String = {
    if (fileName.contains(".")) fileName.split("\\.").last else null
  }

  def matchLocation(m: Regex.Match): Int = {
    m.start + (m.end - m.start) / 2
  }
}
