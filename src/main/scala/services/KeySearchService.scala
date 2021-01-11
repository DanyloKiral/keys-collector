package services

import dto.{ExposedKeyData, RawKeySearchResult}
import scala.util.Random

object KeySearchService {
  def isUsefulSearchResult(record: RawKeySearchResult): Boolean = {
    // test filter statement
    record.lineNum % 2 != 0
  }

  def parse(record: RawKeySearchResult): ExposedKeyData = {
    // test parsing
    new ExposedKeyData(record.lineNum * 2, Random.alphanumeric.take(10).mkString(""), record.service)
  }
}
