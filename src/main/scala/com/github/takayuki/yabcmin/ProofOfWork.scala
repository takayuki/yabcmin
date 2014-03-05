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
import scala.util.hashing.MurmurHash3

class ProofOfWork(val data : Array[Byte], val target : UInt256)
    extends Serializable with StopWatching {
  require(data.length == 80)

  val staticHeader = {
    val staticHeader = new Array[Byte](76)
    Array.copy(data, 0, staticHeader, 0, staticHeader.length)
    staticHeader
  }
  val staticHeaderHashCode = staticHeader.hashCode()

  override def equals(other : Any) = other match {
    case that : ProofOfWork => this.staticHeader sameElements that.staticHeader
    case _ => false
  }

  override def hashCode = MurmurHash3.seqHash(staticHeader)

  override def toString : String = Util.encodeHex(staticHeader)

  def search(startNonce : UInt, count : Long) : (Option[BlockHeader], Long) = {
    val byteBuf = data.clone()
    val intBuf = Util.wrapAsIntBuffer(byteBuf)
    var nonce : Int = startNonce.toInt
    var checked : Long = 0
    val init = MessageDigest.getInstance("SHA-256")
    init.update(byteBuf, 0, 76)
    while (checked < count) {
      val last  = init.clone().asInstanceOf[MessageDigest]
      intBuf.put(19, nonce)
      last.update(byteBuf, 76, 4)
      val hash1 = last.digest()
      last.reset()
      last.update(hash1)
      val hash2 = last.digest()

      if (UInt256(hash2) <= target)
          return (Some(BlockHeader(byteBuf)), checked)

      nonce += 1
      checked += 1
    }
    (None, checked)
  }

}
