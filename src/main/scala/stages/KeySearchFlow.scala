package stages

import akka.NotUsed
import akka.stream.scaladsl.Flow
import constants.Constants
import dto.{ExposedKeyData, FileWithKeyData}

import scala.util.matching.Regex

object KeySearchFlow {
  def apply(): Flow[FileWithKeyData, ExposedKeyData, NotUsed] = {
    shallowSearch
  }

  def shallowSearch(): Flow[FileWithKeyData, ExposedKeyData, NotUsed] = {
    Flow[FileWithKeyData]
      .map(d => (d, searchForSecret(d)))
      .async
      .filter(_._2.size > 0)
      .async
      .mapConcat(d => d._2.distinctBy(m => extractKey(m)).zipAll(List[FileWithKeyData](), null, d._1))
      .async
      .filter(d => isKeyContainNoNoWords(d._1))
      .async
      .map(d => createExposedKeyObject(d))
      .async
  }

  def preciseSearch(): Flow[FileWithKeyData, ExposedKeyData, NotUsed] = {
    Flow[FileWithKeyData]
      .map(d => (d, searchServiceNames(d)))
      .async
      .filter(_._2.size > 0)
      .async
      .mapConcat(d => {
        val res = d._2.map(v => v.toLowerCase).distinct
          .zipAll(List[FileWithKeyData](), null, d._1)
        res
      })
      .async
      .map(d => (Constants.formatSearchRegex(d._1), d._2))
      .async
      .mapConcat(d => d._1.zipAll(List[FileWithKeyData](), null, d._2))
      .async
      .map(d => (d._1, d._2, searchForSecret(d._2.content, d._1._3)))
      .async
      .filter(d => d._3.nonEmpty)
      .async
      .map(d => createExposedKeyObject(d))
      .async
      .mapConcat(list => list)
  }

  def createExposedKeyObject(data: ((String, String, Regex), FileWithKeyData, List[Regex.Match])): List[ExposedKeyData] = {
    if (data._3.size > 1) {
      println("Multiple matches!")
    }

    println("found result")

    data._3.map(k => k.subgroups(0)).distinct
      .map(key => {
        ExposedKeyData(
          data._2.name,
          key,
          Option(data._1._1),
          data._2.html_url,
          data._2.path,
          getFileExtension(data._2.name),
          data._2.sha,
          data._2.repo.url,
          data._2.repo.full_name,
          data._2.repo.html_url,
          None)
    })
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
      data._2.repo.url,
      data._2.repo.full_name,
      data._2.repo.html_url,
      None)
  }

  def searchServiceNames(data: FileWithKeyData): List[String] = {
    Constants.ServiceDetectionRegex.findAllIn(data.content).matchData.toList
      .map(m => m.group((0)))
  }

  def searchServiceName(data: (Regex.Match, FileWithKeyData)): Option[String] = {
    val foundServices = Constants.ServiceDetectionRegex.findAllIn(data._2.content).matchData.toList

    if (foundServices.isEmpty) {
      return None
    }

    val keyLocation = matchLocation(data._1)
    val closestServiceMatch = foundServices.minBy(s => Math.abs(keyLocation - matchLocation(s)))

    Option(closestServiceMatch.group(0).toLowerCase)
  }

  def searchForSecret(content: String, pattern: Regex): List[Regex.Match] = {
    val res = pattern.findAllIn(content).matchData.toList
    res
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
