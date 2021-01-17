package services


import java.time.LocalDateTime

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.Materializer
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.{Sink, Source}
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.ConfigFactory
import constants.Configs
import dto.{DataAnalytics, ExposedKeyData, LanguageExposedKeyStatistics}
import slick.jdbc.{GetResult, PositionedParameters, SetParameter}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

object DataService {
  implicit val session = SlickSession.forConfig(ConfigFactory
    .parseString(
      s"""
        profile = "slick.jdbc.PostgresProfile$$"
        db {
          dataSourceClass = "slick.jdbc.DriverDataSource"
          properties {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://${Configs.PosgresUrl}/${Configs.PosgresDbName}"
            user = "${Configs.PosgresUsername}"
            password = "${Configs.PosgresPassword}"
          }
        }"""))
  import session.profile.api._

  implicit val getStatResult: GetResult[LanguageExposedKeyStatistics] = GetResult[LanguageExposedKeyStatistics](r =>
    LanguageExposedKeyStatistics(r.nextString, r.nextInt, r.nextFloat))

  def initialize()(implicit materializer: Materializer): Unit = {
    Await.result(session.db.run(sql"""
      create schema if not exists key_collector;
      create table if not exists key_collector.exposed_keys (
        id serial primary key,
        file_name varchar(255),
        key varchar(255),
        service varchar(50),
        file_html_url varchar(1000),
        file_path varchar(500),
        language varchar(50),
        sha varchar(50),
        repo_full_name varchar(255),
        repo_html_url varchar(500),
        repo_create_date timestamp,
        created_date timestamp
      );
      DROP View IF EXISTS key_collector.Vw_exposed_keys;
      Create view key_collector.Vw_exposed_keys As
          with Distinct_dataset As
          (Select distinct
          language,
          --service,
          key
          from key_collector.exposed_keys
          Where key not like '%*****%'
          )
      Select distinct
        language,
        --service,
        --key,
        count (key) OVER (PARTITION BY language) as count_by_language,
        (count  (key) OVER (PARTITION BY language)*1.00)/((Select Count (*) from Distinct_dataset)*1.00) * 100 as percentage_by_language
        --,count  (key) OVER (PARTITION BY service) as count_by_service
      from Distinct_dataset;

      select 1
    """.as[Int]), 30 second)
  }

  def insertToDbSink(): Sink[ExposedKeyData, Any] = {
    Slick.sink((keyData: ExposedKeyData) => sql"""
      insert into key_collector.exposed_keys (file_name,
                                              key,
                                              service,
                                              file_html_url,
                                              file_path,
                                              language,
                                              sha,
                                              repo_full_name,
                                              repo_html_url,
                                              repo_create_date,
                                              created_date)
      values (${keyData.file_name},
              ${keyData.key},
              ${keyData.service.getOrElse("null")},
              ${keyData.file_html_url},
              ${keyData.file_path},
              ${keyData.language},
              ${keyData.sha},
              ${keyData.repo_full_name},
              ${keyData.repo_html_url},
              null,
              NOW())
    """.asUpdate)
  }

  def getDataAnalyticsAsJson()(implicit executionContext: ExecutionContextExecutor, mapper: ObjectMapper): Future[String] = {
    session.db.run(
      sql"""
        select
          language,
          count_by_language,
          percentage_by_language
        from key_collector.Vw_exposed_keys
         """.as[LanguageExposedKeyStatistics])
      .map(v => DataAnalytics(v.toList.sortBy(ls => ls.exposures)(Ordering.Int.reverse)))
      .map(mapper.writeValueAsString(_))
  }

  def closeConnection(): Unit = {
    session.close()
  }
}
