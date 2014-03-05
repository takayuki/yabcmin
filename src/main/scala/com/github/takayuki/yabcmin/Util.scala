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

import java.nio.{LongBuffer, IntBuffer, ByteOrder, ByteBuffer}
import org.apache.commons.codec.binary.Hex

object Util {

  def swapInt(array : Array[Byte]) : Array[Byte] = {
    require(array.length % 4 == 0)

    val le = ByteBuffer.wrap(array).order(ByteOrder.LITTLE_ENDIAN)
    val be = ByteBuffer.wrap(array).order(ByteOrder.BIG_ENDIAN)
    while (le.remaining() != 0)
      be.putInt(le.getInt)
    array
  }

  def wrapAsIntBuffer(array : Array[Byte],
                      order: ByteOrder = ByteOrder.LITTLE_ENDIAN)
      : IntBuffer = {
    val le = ByteBuffer.wrap(array).order(order)
    le.asIntBuffer()
  }

  def wrapAsLongBuffer(array : Array[Byte],
                       order: ByteOrder = ByteOrder.LITTLE_ENDIAN)
      : LongBuffer = {
    val le = ByteBuffer.wrap(array).order(order)
    le.asLongBuffer()
  }

  def decodeHex(s : String) : Array[Byte] = Hex.decodeHex(s.toCharArray)

  def encodeHex(a : Array[Byte]) : String = new String(Hex.encodeHex(a))

}
