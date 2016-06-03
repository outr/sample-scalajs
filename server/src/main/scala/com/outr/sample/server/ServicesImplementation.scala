package com.outr.sample.server

import com.outr.sample.Services

import scala.collection.mutable.ListBuffer

object ServicesImplementation extends Services {
  private val history = ListBuffer.empty[String]

  override def reverse(message: String): String = synchronized {
    history += message
    while (history.size > 100) {
      history.trimStart(1)
    }
    message.reverse
  }

  override def history(max: Int): List[String] = history.toList.takeRight(max).reverse
}
