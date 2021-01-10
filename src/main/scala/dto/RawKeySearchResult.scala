package dto

// todo: add here more properties (raw data from GitHub API)
class RawKeySearchResult(val lineNum: Int,
                         val service: String) {

  override def toString: String = s"RawKeySearchResult: {lineNum: $lineNum, keyService: $service}"
}
