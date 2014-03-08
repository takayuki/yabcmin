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

import akka.actor._
import com.ecyrd.speed4j.StopWatch
import com.github.takayuki.yabcmin.{ProofOfWork, UInt}
import com.google.inject.{Inject, Injector}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object Miner {

  case class Config(val masterPath : String,
               val receiveTimeout : FiniteDuration) {
    override def toString =
      s"masterPath=$masterPath, receiveTimeout=$receiveTimeout"
  }

  case class Search(proofOfWork : ProofOfWork,
                    startNonce : UInt,
                    count : Long,
                    startTime : Long)

  def props(injector : Injector) =
    Props { injector.getInstance(classOf[Miner]) }
}

class Miner extends Actor with ActorLogging {

  import context._

  @Inject
  val config : Miner.Config = null

  private def ready() {
    setReceiveTimeout(config.receiveTimeout)
    actorSelection(config.masterPath) ! Master.Ready(self)
  }

  override def preStart() {
    require(config != null)
    log.info("miner: {}", config)
    ready()
  }

  def receive = {
    case Terminated(master)=>
      log.info("terminated {}", master)
      ready()
    case ReceiveTimeout =>
      log.debug("receive timeout")
      ready()
    case Miner.Search(proofOfWork, startNonce, count, startTime) =>
      log.debug("watch {}", sender)
      watch(sender)
      val sw = new StopWatch()
      val (result, checked) = try {
        proofOfWork.search(startNonce, count)
      } finally {
        sw.stop()
      }
      log.debug("checked {} in {}ms", count, sw.getElapsedTime)
      sender ! Master.BlockFound(result,
        startNonce,
        checked,
        startTime,
        FiniteDuration(sw.getTimeMicros, TimeUnit.MICROSECONDS))
  }
}
