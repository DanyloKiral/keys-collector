package dto

// todo: add here more properties (output data model)
class ExposedKeyData(
    val lineNum: Int,
    val key: String,
    val keyService: String) {
  override def toString: String = s"ExposedKeyData: {lineNum: $lineNum, keyService: $keyService}"
}
