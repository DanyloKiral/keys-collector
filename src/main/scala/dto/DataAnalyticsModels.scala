package dto

case class DataAnalytics(languageStatistics: List[LanguageExposedKeyStatistics])
case class LanguageExposedKeyStatistics(language: String, exposures: Int, percentage: Float)
