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

import org.apache.commons.codec.binary.Hex
import scala.util.hashing.MurmurHash3

object UInt256 {
  val bitLength = 256
  val byteLength = bitLength / 8

  def apply() = new UInt256(new Array[Byte](UInt256.byteLength))
  def apply(value : Array[Byte]) = new UInt256(value)
  def apply(value : String) = new UInt256(value)

  def fromCompact(compact : UInt) : UInt256 = {
    val value = {
      val value = new Array[Byte](UInt256.byteLength)
      val exp = compact >>> 24
      val man = compact & UInt(0x7fffff)
      val sign = compact & UInt(0x800000)
      require(exp.toInt > 3)
      require(sign.toInt == 0)
      val offset = exp.toInt - 3
      if (offset < UInt256.byteLength)
        value(offset) = man.toByte
      if (offset + 1 < UInt256.byteLength)
        value(offset + 1) = (man >>> 8).toByte
      if (offset + 2 < UInt256.byteLength)
        value(offset + 2) = ((man >>> 16) & UInt(0x7f)).toByte
      value
    }
    new UInt256(value)
  }
}

class UInt256(digits : Array[Byte]) extends Serializable
    with Ordered[UInt256] {
  require(digits.length == UInt256.byteLength)

  def this(s : String) = this(Hex.decodeHex(s.toCharArray))

  def array : Array[Byte] = digits

  override def compare(that : UInt256) : Int = {
    var i = UInt256.byteLength - 1
    while (i >= 0 && UByte.eq(that.array(i), this.array(i)))
      i -= 1
    if (i >= 0)
      UByte.cmp(this.array(i), that.array(i))
    else
      0
  }

  def canEqual(other : Any) = other.isInstanceOf[UInt256]

  override def equals(other : Any) = other match {
    case that : UInt256 =>
      that.canEqual(this) && this.array.sameElements(that.array)
    case _ =>
      false
  }

  override def hashCode = MurmurHash3.seqHash(digits)

  override def toString = new String(Hex.encodeHex(digits))
}
