package utils

import java.nio.file.{Paths, Files, Path}

import resource._

class TemporaryFile(val path: Path) {
  def delete(): Unit = Files.delete(path)
}

object TemporaryFile {
  def create(prefix: String, suffix: String = ".tmp"): TemporaryFile = {
    new TemporaryFile(Files.createTempFile(prefix, suffix))
  }

  def createWithContentsFromBytes(contents: Array[Byte], prefix: String, suffix: String = ".tmp"): TemporaryFile = {
    val file = create(prefix, suffix)
    Files.write(file.path, contents)
    file
  }

  def createWithContents(contents: String, prefix: String, suffix: String = ".tmp"): TemporaryFile = {
    createWithContentsFromBytes(contents.getBytes("UTF-8"), prefix, suffix)
  }

  implicit object TemporaryFileResource extends Resource[TemporaryFile] {
    override def close(r: TemporaryFile): Unit = r.delete()
  }
}
