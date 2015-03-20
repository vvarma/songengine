package com.nvr

import java.io.{FileWriter, PrintWriter}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.nvr.songengine.player.PathConstants

/**
 * Created by vinay.varma on 2/22/15.
 */
package object songengine {

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  val songStore = PathConstants.HOME + "/songs.json"
  val eventStore = PathConstants.HOME + "/events.json"

  def using[A <: {def close() : Unit}, B](param: A)(f: A => B): B =
    try {
      f(param)
    } finally {
      param.close()
    }

  def appendToFile(fileName: String, textData: String) =
    using(new FileWriter(fileName, true)) {
      fileWriter => using(new PrintWriter(fileWriter)) {
        printWriter => printWriter.println(textData)
      }
    }

  def serialize(data: AnyRef): String = {
    mapper.writeValueAsString(data)
  }

}
