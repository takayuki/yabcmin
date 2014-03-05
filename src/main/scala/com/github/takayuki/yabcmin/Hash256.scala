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

import java.security.MessageDigest
import org.apache.commons.codec.binary.Hex

object Hash256 {

  def apply(s : String) : UInt256 = Hash256(Hex.decodeHex(s.toCharArray))

  def apply(buf : Array[Byte]) : UInt256 = Hash256(buf, 0, buf.length)

  def apply(buf : Array[Byte], offset : Int, len : Int) : UInt256 = {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(buf, offset, len)
    val hash1 = md.digest()
    md.reset()
    md.update(hash1)
    val hash2 = md.digest()
    UInt256(hash2)
  }

}
