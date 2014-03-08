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
package com.github.takayuki.yabcmin.rpc

import com.github.takayuki.yabcmin.BlockHeader

class MockClient extends ClientInterface {

  override def getwork(): Option[GetWork] = {
    GetWork.fromJson("""{"result":{"data":"000000020a8ce26f72b3f1b646a2a6c14ff763ae65831e939c085ae10019d66800000000772642b14b6bfae89f9e3e07dbf3c929ac5c29171b4f5d40b2fd505ef241c57152fc759e1d0fffff00000000000000800000000000000000000000000000000000000000000000000000000000000000000000000000000080020000", "target":"0000000000000000000000000000000000000000000000000000ffff0f000000"},"error":null}""")
  }

  override def getwork(blockHeader: BlockHeader) : Boolean = {
    true
  }

}
