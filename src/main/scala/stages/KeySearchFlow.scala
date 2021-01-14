package stages

import akka.NotUsed
import akka.stream.scaladsl.Flow
import dto.{ExposedKeyData, FileWithKeyData}

object KeySearchFlow {
  def apply(): Flow[FileWithKeyData, ExposedKeyData, NotUsed] = {
    Flow[FileWithKeyData]
      .map(d => ExposedKeyData(d.name, d.path, d.sha, d.repo.full_name, d.repo.html_url))
  }
}
