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
class BlockHashSuite extends FunSuite with Matchers {

  test("256-bit interger representation") {
    assert(UInt256().toString == "0000000000000000000000000000000000000000000000000000000000000000")
    assert(UInt256("0123456789abcdef000000000000000000000000000000000000000000000000").toString == "0123456789abcdef000000000000000000000000000000000000000000000000")
    assert(UInt256("0000000000000000000000000000000000000000000000000123456789abcdef").toString == "0000000000000000000000000000000000000000000000000123456789abcdef")
  }

  test("256-bit integer equality") {
    assert(UInt256() == UInt256("0000000000000000000000000000000000000000000000000000000000000000"))
    assert(UInt256() != UInt256("1000000000000000000000000000000000000000000000000000000000000000"))
    assert(UInt256("0123456789abcdef000000000000000000000000000000000000000000000000") == UInt256("0123456789abcdef000000000000000000000000000000000000000000000000"))
    assert(UInt256("0000000000000000000000000000000000000000000000000123456789abcdef") == UInt256("0000000000000000000000000000000000000000000000000123456789abcdef"))
    assert(UInt256().hashCode == UInt256().hashCode)
  }

  test("256-bit integer comparison") {
    assert(UInt256() <= UInt256() && !(UInt256() < UInt256()))
    assert(UInt256() < UInt256("1000000000000000000000000000000000000000000000000000000000000000"))
  }

  // See https://en.bitcoin.it/wiki/Block_hashing_algorithm
  val rawBlockHeader = Util.decodeHex("0100000081cd02ab7e569e8bcd9317e2fe99f2de44d49ab2b8851ba4a308000000000000e320b6c2fffc8d750423db8b1eb942ae710e951ed797f7affc8892b0f1fc122bc7f5d74df2b9441a42a14695")

  test("double SHA-256 hash value") {
    assert(Hash256(rawBlockHeader) == UInt256("1dbd981fe6985776b644b173a4d0385ddc1aa2a829688d1e0000000000000000"))
  }

}
