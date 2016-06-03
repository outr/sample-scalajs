package com.outr.sample.client

import org.scalajs.dom
import upickle.{Js, default}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

object ClientServices extends autowire.Client[Js.Value, upickle.default.Reader, upickle.default.Writer] {
  override def write[Result](r: Result)(implicit evidence$2: default.Writer[Result]): Js.Value = upickle.default.writeJs(r)

  override def read[Result](p: Js.Value)(implicit evidence$1: default.Reader[Result]): Result = upickle.default.readJs[Result](p)

  override def doCall(req: ClientServices.Request): Future[Js.Value] = {
    val data = upickle.json.write(Js.Obj(req.args.toSeq: _*))
    dom.ext.Ajax.post(
      url = s"/service/${req.path.mkString("/")}",
      data = data
    ).map(_.responseText).map(upickle.json.read)
  }
}
