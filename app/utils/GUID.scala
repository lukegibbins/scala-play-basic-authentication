package utils

import java.util.UUID

trait GUID {
  def generateGUID(): String = {
    UUID.randomUUID().toString
  }
}
