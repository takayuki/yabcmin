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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}

@RunWith(classOf[JUnitRunner])
class UnsignedValueSuite extends FunSuite with Matchers {

  test("unsigned byte mapping to byte") {
    assert(UByte(-1) == -1)
    assert(UByte(0) == 0)
    assert(UByte(1) == 1)
    assert(UByte(127) == 127)
    assert(UByte(128) == -128)
    assert(UByte(129) == -127)
    assert(UByte(255) == -1)
    assert(UByte(256) == 0)
    assert(UByte(257) == 1)
  }

  test("unsigned byte representation") {
    assert(UByte.toInt(UByte(-1)) == 255)
    assert(UByte.toInt(UByte(0)) == 0)
    assert(UByte.toInt(UByte(1)) == 1)
    assert(UByte.toInt(UByte(127)) == 127)
    assert(UByte.toInt(UByte(128)) == 128)
    assert(UByte.toInt(UByte(129)) == 129)
    assert(UByte.toInt(UByte(255)) == 255)
    assert(UByte.toInt(UByte(256)) == 0)
    assert(UByte.toInt(UByte(257)) == 1)
  }

  test("unsigned byte equality") {
    assert(UByte.ne(UByte(0), UByte(1)))
    assert(UByte.eq(UByte(-1), UByte(-1)))
    assert(UByte.eq(UByte(0), UByte(0)))
    assert(UByte.eq(UByte(1), UByte(1)))
    assert(UByte.eq(UByte(255), UByte(-1)))
    assert(UByte.eq(UByte(256), UByte(0)))
    assert(UByte.eq(UByte(257), UByte(1)))
  }

  test("unsigned byte comparison") {
    assert(UByte.le(UByte(0), UByte(0)))
    assert(UByte.lt(UByte(0), UByte(1)))
    assert(UByte.lt(UByte(0), UByte(128)))
    assert(UByte.lt(UByte(0), UByte(255)))
    assert(UByte.le(UByte(1), UByte(1)))
    assert(UByte.lt(UByte(1), UByte(2)))
    assert(UByte.lt(UByte(1), UByte(128)))
    assert(UByte.lt(UByte(1), UByte(255)))
    assert(UByte.le(UByte(127), UByte(127)))
    assert(UByte.lt(UByte(127),UByte(128)))
    assert(UByte.le(UByte(128), UByte(128)))
    assert(UByte.lt(UByte(128), UByte(255)))
    assert(UByte.le(UByte(255), UByte(255)))
    assert(UByte.ge(UByte(0), UByte(0)))
    assert(UByte.gt(UByte(1), UByte(0)))
    assert(UByte.gt(UByte(128), UByte(0)))
    assert(UByte.gt(UByte(255), UByte(0)))
    assert(UByte.ge(UByte(1), UByte(1)))
    assert(UByte.gt(UByte(2), UByte(1)))
    assert(UByte.gt(UByte(128), UByte(1)))
    assert(UByte.gt(UByte(255), UByte(1)))
    assert(UByte.ge(UByte(127), UByte(127)))
    assert(UByte.gt(UByte(128), UByte(127)))
    assert(UByte.ge(UByte(128), UByte(128)))
    assert(UByte.gt(UByte(255), UByte(128)))
    assert(UByte.ge(UByte(255), UByte(255)))
  }

  test("unsigned int mapping to int") {
    assert(UInt(-1).toInt == -1)
    assert(UInt(0).toInt == 0)
    assert(UInt(1).toInt == 1)
    assert(UInt((1L << 31) - 1).toInt == 2147483647)
    assert(UInt((1L << 31)).toInt == -2147483648)
    assert(UInt((1L << 31) + 1).toInt == -2147483647)
    assert(UInt((1L << 32) - 1).toInt == -1)
    assert(UInt((1L << 32)).toInt == 0)
    assert(UInt((1L << 32) + 1).toInt == 1)
  }

  test("unsigned int representation") {
    assert(UInt(-1).toLong == 4294967295L)
    assert(UInt(0).toInt == 0)
    assert(UInt(1).toInt == 1)
    assert(UInt(2147483647L).toLong == 2147483647L)
    assert(UInt(2147483648L).toLong == 2147483648L)
    assert(UInt(2147483649L).toLong == 2147483649L)
    assert(UInt(4294967295L).toLong == 4294967295L)
    assert(UInt(4294967296L).toLong == 0)
    assert(UInt(4294967297L).toLong == 1)
  }

  test("unsigned int equality") {
    assert(UInt(0) != UInt(1))
    assert(UInt(-1) == UInt(-1))
    assert(UInt(0) == UInt(0))
    assert(UInt(1) == UInt(1))
    assert(UInt(4294967295L) == UInt(-1))
    assert(UInt(4294967296L) == UInt(0))
    assert(UInt(4294967297L) == UInt(1))
    assert(UInt(0).hashCode != UInt(1).hashCode)
    assert(UInt(0).hashCode == UInt(0).hashCode)
  }

}
