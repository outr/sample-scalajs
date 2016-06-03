package com.outr.sample.client

import autowire._
import com.outr.sample.Services
import com.outr.scribe.Logging

import scala.scalajs.js.JSApp
import scala.util.{Failure, Success}
import org.scalajs.dom._
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLInputElement, HTMLTextAreaElement}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object Client extends JSApp with Logging {
  lazy val services = ClientServices[Services]

  lazy val reverseInput = document.getElementById("reverseInput").asInstanceOf[HTMLInputElement]
  lazy val reversed = document.getElementById("reversed").asInstanceOf[HTMLInputElement]
  lazy val history = document.getElementById("history").asInstanceOf[HTMLTextAreaElement]
  lazy val loadHistoryButton = document.getElementById("loadHistoryButton").asInstanceOf[HTMLButtonElement]

  override def main(): Unit = {
    logger.info("Scala.js client started...")

    reverseInput.addEventListener("input", { (evt: Event) =>
      reverseMessage(reverseInput.value)
    }, useCapture = true)

    loadHistoryButton.addEventListener("click", { (evt: Event) =>
      loadHistory()
    }, useCapture = true)
  }

  def reverseMessage(message: String): Unit = {
    services.reverse(message).call().onComplete {
      case Success(response) => reversed.value = response
      case Failure(t) => logger.error(s"Server failure while reversing message: ${t.getMessage}.")
    }
  }

  def loadHistory(): Unit = {
    services.history(10).call().onComplete {
      case Success(response) => history.value = response.mkString("\n")
      case Failure(t) => logger.error(s"Server failure while loading history: ${t.getMessage}.")
    }
  }
}