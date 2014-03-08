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
import com.github.takayuki.yabcmin.BlockHeader
import com.github.takayuki.yabcmin.miner.Loader.ScheduledTask
import com.github.takayuki.yabcmin.rpc.ClientInterface
import com.google.inject.{Inject, Injector}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object Loader {

  case class Load(delay : FiniteDuration)
  case class Submit(blockHeader : BlockHeader,
                    origin : (Master.BlockFound, ActorRef))

  def props(injector : Injector) =
    Props { injector.getInstance(classOf[Loader]) }

  class ScheduledTask(scheduler : Scheduler) {
    private[this] var scheduledTask : Option[Cancellable] = None

    def schedule(delay : FiniteDuration)(task : => Unit)
                (implicit executor: ExecutionContext) = {
      if (scheduledTask.nonEmpty)
        scheduledTask.get.cancel()
      scheduledTask = Some(scheduler.scheduleOnce(delay)(task))
    }
  }
}

class Loader extends Actor with ActorLogging {

  import context._

  @Inject
  val injector : Injector = null

  @Inject
  val client : ClientInterface = null

  private[this] val sched = new ScheduledTask(system.scheduler)

  override def preStart() = {
    require(client != null)
    actorOf(Master.props(injector), "master")
  }

  private def load(delay : FiniteDuration) = {
    log.info("schedule to get a new block in {} secs", delay.toSeconds)
    val master = sender
    sched.schedule(delay) {
      master ! Master.NewWork(client.getwork())
    }
  }

  override def receive = {
    case Terminated(master) =>
      log.info("terminated {}", master)
    case Loader.Load(delay) =>
      load(delay)
    case Loader.Submit(blockHeader, origin) =>
      if (client.getwork(blockHeader))
        sender ! Master.Accepted(blockHeader, origin)
      else
        sender ! Master.Rejected(blockHeader, origin)
  }
}
