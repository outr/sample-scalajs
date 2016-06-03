package com.outr.sample.server

import upickle._
import upickle.default._

object ServerServices extends autowire.Server[Js.Value, Reader, Writer] {
  override def read[Result](p: Js.Value)(implicit evidence$1: default.Reader[Result]): Result = default.readJs[Result](p)

  override def write[Result](r: Result)(implicit evidence$2: default.Writer[Result]): Js.Value = default.writeJs(r)
}