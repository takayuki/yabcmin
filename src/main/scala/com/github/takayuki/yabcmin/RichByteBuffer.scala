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
package com.github.takayuki.yabcmin

import java.nio.ByteBuffer

object RichByteBuffer {

  import scala.language.implicitConversions

  implicit def byteBufferToRichByteBuffer(buf : ByteBuffer) : RichByteBuffer =
    new RichByteBuffer(buf)
}

class RichByteBuffer(buf : ByteBuffer) {

  def getUInt : UInt = new UInt(buf.getInt)

  def getUInt256 : UInt256 = {
    val value = new Array[Byte](UInt256.byteLength)
    buf.get(value)
    new UInt256(value)
  }

  def putUInt(value : UInt) {
    buf.putInt(value.toInt)
  }

  def putUInt256(value : UInt256) {
    buf.put(value.array)
  }

}
