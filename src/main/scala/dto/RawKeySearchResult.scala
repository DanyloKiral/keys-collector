package dto

// todo: add here more properties (raw data from GitHub API)
class RawKeySearchResult(val lineNum: Int,
                         val keyService: String) {

  override def toString: String = s"RawKeySearchResult: {lineNum: $lineNum, keyService: $keyService}"
}
