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

object UByte {
  val MAX_VALUE = 255
  val MIN_VALUE = 0
  val SIZE = 8

  def apply(value : Long) : Byte = (value & UByte.MAX_VALUE).toByte

  def toInt(value : Byte) : Int = value.toInt & UByte.MAX_VALUE

  def cmp(left : Byte, right : Byte) : Int =  toInt(left) - toInt(right)

  def eq(left : Byte, right : Byte) = cmp(left, right) == 0

  def ne(left : Byte, right : Byte) = !eq(left, right)

  def lt(left : Byte, right : Byte) = cmp(left, right) < 0

  def le(left : Byte, right : Byte) = lt(left, right) || eq(left, right)

  def gt(left : Byte, right : Byte) = !le(left, right)

  def ge(left : Byte, right : Byte) = !lt(left, right)
}
