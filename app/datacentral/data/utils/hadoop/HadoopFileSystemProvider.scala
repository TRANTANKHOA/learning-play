package datacentral.data.utils.hadoop

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem

trait HadoopFileSystemProvider {

  @transient protected lazy val fs: FileSystem = {
    val x = new Configuration
    FileSystem.get(x)
  }
}
