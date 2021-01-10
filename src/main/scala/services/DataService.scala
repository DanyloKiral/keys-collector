package services

import akka.stream.Materializer
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import dto.ExposedKeyData
import scala.concurrent.Await
import scala.concurrent.duration._

object DataService {
  implicit val session = SlickSession.forConfig(ConfigFactory
    .parseString(
      """
        profile = "slick.jdbc.PostgresProfile$"
        db {
          dataSourceClass = "slick.jdbc.DriverDataSource"
          properties {
            driver = "org.postgresql.Driver"
            url = "jdbc:postgresql://localhost:5432/danylokiral"
            user = "danylokiral"
            password = ""
          }
        }"""))
  // url -> danylokiral is a database name
  import session.profile.api._

  def initialize()(implicit materializer: Materializer): Unit = {
    Await.result(session.db.run(sql"""
      create schema if not exists key_collector;

      drop table if exists key_collector.exposed_keys;
      create table key_collector.exposed_keys (
        id serial primary key,
        line_num int,
        key varchar(1000),
        service varchar(50)
      );

      select 1
    """.as[Int]), Duration(30, SECONDS))
  }

  def insertToDbSink(): Sink[ExposedKeyData, Any] = {
    Slick.sink((keyData: ExposedKeyData) => sql"""
      insert into key_collector.exposed_keys (line_num, key, service) values (${keyData.lineNum}, ${keyData.key}, ${keyData.service})
    """.asUpdate)
  }

  def analyzeData(): Unit = {
    ???
  }

  def closeConnection(): Unit = {
    session.close()
  }
}
