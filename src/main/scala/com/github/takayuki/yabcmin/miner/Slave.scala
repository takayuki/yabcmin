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
package com.github.takayuki.yabcmin.miner

import akka.actor.{Terminated, Props, Actor, ActorLogging}
import akka.remote.RemotingLifecycleEvent
import com.google.inject.{Inject, Injector}

object Slave {

  case class Config(val miners : Int) {
    override def toString = s"miners=$miners"
  }

  def props(injector : Injector) =
    Props { injector.getInstance(classOf[Slave]) }

}

class Slave extends Actor with ActorLogging {

  import context._

  @Inject
  val config : Slave.Config = null

  @Inject
  val injector : Injector = null

  override def preStart() = {
    require(config != null)
    log.info("slave: {}", config)
    (0 until config.miners).foreach {
      id => actorOf(Miner.props(injector), s"miner$id")
    }
    system.eventStream.subscribe(self, classOf[RemotingLifecycleEvent])
  }

  def receive = {
    case Terminated(miner) =>
      log.info("terminated {}", miner)
    case e : RemotingLifecycleEvent =>
      log.info("remoting lifecycle: {}", e)
  }
}
