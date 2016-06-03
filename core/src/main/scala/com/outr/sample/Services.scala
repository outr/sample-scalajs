package com.outr.sample

trait Services {
  def reverse(message: String): String

  def history(max: Int): String
}