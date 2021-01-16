package services

import akka.stream.Materializer
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import constants.Configs
import dto.ExposedKeyData

import scala.concurrent.Await
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

  def initialize()(implicit materializer: Materializer): Unit = {
    Await.result(session.db.run(sql"""
      create schema if not exists key_collector;

      drop table if exists key_collector.exposed_keys;
      create table key_collector.exposed_keys (
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
        repo_create_date varchar(50)
      );

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
                                              repo_create_date)
      values (${keyData.file_name},
              ${keyData.key},
              ${keyData.service.getOrElse("null")},
              ${keyData.file_html_url},
              ${keyData.file_path},
              ${keyData.language},
              ${keyData.sha},
              ${keyData.repo_full_name},
              ${keyData.repo_html_url},
              ${keyData.repo_create_date})
    """.asUpdate)
  }

  def analyzeData(): Unit = {
    ???
  }

  def closeConnection(): Unit = {
    session.close()
  }
}
