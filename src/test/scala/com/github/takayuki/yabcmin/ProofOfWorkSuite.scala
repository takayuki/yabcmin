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

import com.github.takayuki.yabcmin.rpc.GetWork
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FunSuite}

@RunWith(classOf[JUnitRunner])
class ProofOfWorkSuite extends FunSuite with Matchers {
  // See https://en.bitcoin.it/wiki/Getwork
  val getWork = new GetWork("000000020597ba1f0cd423b2a3abb0259a54ee5f783077a4ad45fb6200000218000000008348d1339e6797e2b15e9a3f2fb7da08768e99f02727e4227e02903e43a42b31511553101a051f3c00000000000000800000000000000000000000000000000000000000000000000000000000000000000000000000000080020000", "00000000000000000000000000000000000000000000003c1f05000000000000")

  test("Getwork representation") {
    assert(getWork.data sameElements Util.decodeHex("020000001fba9705b223d40c25b0aba35fee549aa477307862fb45ad180200000000000033d14883e297679e3f9a5eb108dab72ff0998e7622e427273e90027e312ba443105315513c1f051a00000000800000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000280"))
  }

  test("ProofOfWork representation") {
    val proofOfWork = getWork.proofOfWork
    assert(proofOfWork.data sameElements Util.decodeHex("020000001fba9705b223d40c25b0aba35fee549aa477307862fb45ad180200000000000033d14883e297679e3f9a5eb108dab72ff0998e7622e427273e90027e312ba443105315513c1f051a00000000"))
  }

  // See https://en.bitcoin.it/wiki/Block_hashing_algorithm
  val rawBlockHeader = Util.decodeHex("0100000081cd02ab7e569e8bcd9317e2fe99f2de44d49ab2b8851ba4a308000000000000e320b6c2fffc8d750423db8b1eb942ae710e951ed797f7affc8892b0f1fc122bc7f5d74df2b9441a42a14695")

  test("BlockHeader representation") {
    assert(rawBlockHeader sameElements new BlockHeader(1, UInt256("81cd02ab7e569e8bcd9317e2fe99f2de44d49ab2b8851ba4a308000000000000"), UInt256("e320b6c2fffc8d750423db8b1eb942ae710e951ed797f7affc8892b0f1fc122b"), UInt(0x4dd7f5c7), UInt(0x1a44b9f2), UInt(0x9546a142)).byteArray)
  }

  test("block hash search") {
    val blockHeader = BlockHeader(rawBlockHeader)
    val target = UInt256.fromCompact(blockHeader.bits)
    assert(target == UInt256("0000000000000000000000000000000000000000000000f2b944000000000000"))
    assert(Hash256(rawBlockHeader) < target)
    val proofOfWork = new ProofOfWork(new BlockHeader(1, UInt256("81cd02ab7e569e8bcd9317e2fe99f2de44d49ab2b8851ba4a308000000000000"), UInt256("e320b6c2fffc8d750423db8b1eb942ae710e951ed797f7affc8892b0f1fc122b"), UInt(0x4dd7f5c7), UInt(0x1a44b9f2), UInt(0)).byteArray, target)
    proofOfWork.search(UInt(2504433000L), 1000) match {
      case (Some(found), _) =>
        assert(found.nonce == UInt(2504433986L))
      case (None, _) =>
        fail()
    }
  }
}
