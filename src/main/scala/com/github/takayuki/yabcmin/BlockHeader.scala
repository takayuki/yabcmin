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

import java.nio.{ByteOrder, ByteBuffer}

object BlockHeader {

  def apply(data : Array[Byte]) = {
    import RichByteBuffer.byteBufferToRichByteBuffer

    val buf = ByteBuffer.wrap(data)
    buf.order(ByteOrder.LITTLE_ENDIAN)
    val version = buf.getInt
    val hashPrevBlock = buf.getUInt256
    val hashMerkleRoot = buf.getUInt256
    val time = buf.getUInt
    val bits = buf.getUInt
    val nonce = buf.getUInt

    new BlockHeader(version, hashPrevBlock, hashMerkleRoot, time, bits, nonce)
  }
}

class BlockHeader(val version : Int,
                  val hashPrevBlock : UInt256,
                  val hashMerkleRoot : UInt256,
                  val time : UInt,
                  val bits : UInt,
                  val nonce : UInt) extends Serializable {

  def byteArray : Array[Byte] = {
    import RichByteBuffer.byteBufferToRichByteBuffer

    val buf = ByteBuffer.allocate(80)
    buf.order(ByteOrder.LITTLE_ENDIAN)
    buf.putInt(version)
    buf.putUInt256(hashPrevBlock)
    buf.putUInt256(hashMerkleRoot)
    buf.putUInt(time)
    buf.putUInt(bits)
    buf.putUInt(nonce)
    buf.array()
  }

  override def toString =
    "BlockHeader(ver=%d, hashPrevBlock=%s, hashMerkleRoot=%s, time=%d, bits=%x, nonce=%d)"
      .format(version, hashPrevBlock, hashMerkleRoot, time.toLong, bits.toInt, nonce.toLong)

}
