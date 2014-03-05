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

object UInt {
  val MAX_VALUE = (1L << 32) - 1
  val MIN_VALUE = 0
  val SIZE = 32

  def apply(value : Long) = new UInt(value)
}

class UInt(value : Int) extends Serializable with Ordered[UInt] {

  def this(value : Long) = this((value & UInt.MAX_VALUE).toInt)

  def >>>(shift : Int) : UInt = new UInt(value >>> shift)

  def &(other : UInt) : UInt = new UInt(value & other.toInt)

  def toByte : Byte = (value & 0xff).toByte

  def toInt : Int = value

  def toLong : Long = value.toLong & UInt.MAX_VALUE

  override def compare(that : UInt) :Int = (this.toLong - that.toLong).toInt

  def canEqual(other : Any) = other.isInstanceOf[UInt]

  override def equals(other : Any) = other match {
    case that : UInt => that.canEqual(this) && this.toInt == that.toInt
    case _ => false
  }

  override def hashCode = value.hashCode

  override def toString = (value.toLong & UInt.MAX_VALUE).toString
}
