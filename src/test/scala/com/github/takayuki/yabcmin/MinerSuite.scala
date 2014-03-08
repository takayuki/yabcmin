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

import akka.actor.ActorRef
import akka.testkit.TestProbe
import com.github.takayuki.yabcmin.miner.{Master, Miner, Loader}
import com.github.takayuki.yabcmin.rpc.{MockClient, ClientInterface}
import com.google.inject.name.Names
import com.google.inject.{Injector, AbstractModule, Guice}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class MinerSuite extends AbstractTestKit {

  test("miner actor") {
    val injector = Guice.createInjector(new AbstractModule {
      override def configure() = {
        val minerConfig = new Miner.Config("..", 60.seconds)
        bind(classOf[Miner.Config]).toInstance(minerConfig)
      }
    })
    val client = new MockClient()
    client.getwork() match {
      case Some(getWork) =>
        val proofOfWork = getWork.proofOfWork
        val miner = system.actorOf(Miner.props(injector))
        miner ! Miner.Search(proofOfWork, UInt(36451000L), 1000, 0)
        expectMsgClass(classOf[Master.BlockFound]) match {
          case Master.BlockFound(Some(found), _, _, _, _) =>
            assert(found.nonce == UInt(36451451L))
          case Master.BlockFound(None, _, _, _, _) =>
            fail()
        }
      case None =>
        fail()
    }
  }

  test("master miner") {
    val probe = new TestProbe(system)
    val injector : Injector = Guice.createInjector(new AbstractModule {
      override def configure(): Unit = {
        bind(classOf[Master.Config])
          .toInstance(new Master.Config(4, 1000000, 0.seconds))
        bind(classOf[Miner.Config])
          .toInstance(new Miner.Config("..", 60.seconds))
        bind(classOf[ClientInterface])
          .to(classOf[MockClient])
        bind(classOf[ActorRef])
          .annotatedWith(Names.named("MasterMinerProbe"))
          .toInstance(probe.ref)
      }})
    system.actorOf(Loader.props(injector), "test-loader")

    probe.expectMsgClass(60.seconds, classOf[Master.BlockFound]) match {
      case Master.BlockFound(Some(found), _, _, _, _) =>
        assert(found.nonce == UInt(36451451L))
    }
  }

}
