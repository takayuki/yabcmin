/*
 * Copyright (c) 2014 Takayuki Usui
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.takayuki.yabcmin.rpc

import com.github.takayuki.yabcmin._
import java.nio.ByteOrder
import scala.Some
import scala.util.parsing.json.JSON
import scala.util.parsing.json.JSONArray
import scala.util.parsing.json.JSONObject

object GetWork {

  def fromJson(json : String) : Option[GetWork] = {
    JSON.parseFull(json) match {
      case Some(j) =>
        val result = j.asInstanceOf[Map[String, Any]]("result")
          .asInstanceOf[Map[String, Any]]
        Some(new GetWork(result("data").asInstanceOf[String],
                         result("target").asInstanceOf[String]))
      case _ => None
    }
  }

  def toJson : JSONObject = {
    JSONObject(Map("method" -> "getwork", "params" -> JSONArray(List())))
  }

  def toJson(blockHeader : BlockHeader) : JSONObject = {
    val rawBlockHeader = blockHeader.byteArray
    val param = new Array[Byte](128)
    Array.copy(Util.swapInt(rawBlockHeader), 0,
               param, 0, rawBlockHeader.length)
    Array.copy(Util.decodeHex("000000800000000000000000000000000000000000000000000000000000000000000000000000000000000080020000"),
               0, param, rawBlockHeader.length, 48)
    JSONObject(Map("method" -> "getwork",
                   "params" -> JSONArray(List(Util.encodeHex(param)))))
  }
}

class GetWork(val data : Array[Byte], val target : UInt256)
    extends Serializable {
  require(data.length == 128)
  require(Util.wrapAsLongBuffer(data, ByteOrder.BIG_ENDIAN).get(15) == 640)
  require(UInt256.fromCompact(UInt(Util.wrapAsIntBuffer(data).get(18))) == target)

  def this(data : String, target : String) =
    this(Util.swapInt(Util.decodeHex(data)), UInt256(Util.decodeHex(target)))

  def blockHeader = {
    val data = new Array[Byte](80)
    this.data.copyToArray(data)
    BlockHeader(data)
  }

  def proofOfWork = {
    val data = new Array[Byte](80)
    this.data.copyToArray(data)
    val proofOfWork = new ProofOfWork(data, target)
    proofOfWork
  }
}
