package dto

// todo: add here more properties (output data model)
class ExposedKeyData(
    val lineNum: Int,
    val key: String,
    val service: String) {
  override def toString: String = s"ExposedKeyData: {lineNum: $lineNum, key: $key, service: $service}"
}
