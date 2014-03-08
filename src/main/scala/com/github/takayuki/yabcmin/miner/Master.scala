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
import akka.remote.RemotingLifecycleEvent
import com.codahale.metrics.{Gauge, MetricRegistry}
import com.github.takayuki.yabcmin._
import com.github.takayuki.yabcmin.rpc.GetWork
import com.google.inject.name.Named
import com.google.inject.{Inject, Injector}
import javax.annotation.Nullable
import scala.Some
import scala.concurrent.duration._

object Master {

  case class Config(miners : Int,
                    unit : Long,
                    refresh : FiniteDuration) {
    override def toString = s"miners=$miners, unit=$unit, refresh=$refresh"
  }

  case class Ready(actor : ActorRef)
  case class NewWork(getWork : Option[GetWork])
  case class BlockFound(found : Option[BlockHeader],
                        startNonce : UInt,
                        checked : Long,
                        startTime : Long,
                        elapsed : Duration)
  case class Accepted(blockHeader : BlockHeader,
                      origin : (Master.BlockFound, ActorRef))
  case class Rejected(blockHeader : BlockHeader,
                      origin : (Master.BlockFound, ActorRef))

  def props(injector : Injector) =
    Props { injector.getInstance(classOf[Master]) }

  class Metrics @Inject()
  (val config: Config, val registry : MetricRegistry) {
    registry.register("Miners", new Gauge[Int]{
      override def getValue: Int = config.miners
    })
    registry.register("Unit", new Gauge[Long]{
      override def getValue: Long = config.unit
    })
    registry.register("Refresh", new Gauge[Long]{
      override def getValue: Long = config.refresh.toSeconds
    })
    val hashRateHist = registry.histogram("HashRate")
    val hashTimeHist = registry.histogram("HashTime")
    val deliveryTimeHist = registry.histogram("DeliveryTime")
    val acceptCount = registry.counter("Accepted")
    val rejectCount = registry.counter("Rejected")

    def hashRate(rate : Long) : Unit = hashRateHist.update(rate)
    def hashTime(micros : Long) : Unit = hashTimeHist.update(micros)
    def deliveryTime(micros : Long) :Unit = deliveryTimeHist.update(micros)
    def accepted() = acceptCount.inc()
    def rejected() = rejectCount.inc()
  }
}

class Master extends Actor with ActorLogging {

  import context._

  @Inject
  val injector : Injector = null

  @Inject
  val config : Master.Config = null

  @Inject
  val metrics : Master.Metrics = null

  @Inject
  @Named("MasterMinerProbe")
  @Nullable
  val probe : ActorRef = null

  private[this] val loader = context.parent
  private[this] var proofOfWork : Option[ProofOfWork] = None
  private[this] var nonce : Long = 0
  private[this] val miners = scala.collection.mutable.Set[ActorRef]()
  private[this] val active = scala.collection.mutable.Set[ActorRef]()

  private def load(delay : FiniteDuration = 0.seconds) = {
    loader ! Loader.Load(delay)
  }

  override def preStart() = {
    require(config != null)
    log.info("master: {}", config)
    (0 until config.miners).map {
      id => actorOf(Miner.props(injector), s"miner$id")
    }
    system.eventStream.subscribe(self, classOf[RemotingLifecycleEvent])
    load()
  }

  private def submit(blockHeader : BlockHeader,
                    origin : (Master.BlockFound, ActorRef)) = {
    val (_, miner) = origin
    require(active.contains(miner))
    active.remove(miner)
    loader ! Loader.Submit(blockHeader, origin)
  }

  private def dispatch(miner : ActorRef) = {
    if (proofOfWork.nonEmpty) {
      miner ! Miner.Search(proofOfWork.get, UInt(nonce),
        config.unit, System.nanoTime())
      nonce += config.unit
      active += miner
    }
  }

  def receive = {
    case Terminated(miner) =>
      log.info("terminated {}", miner)
      if (miners.contains(miner))
        miners.remove(miner)
      if (active.contains(miner))
        active.remove(miner)
      require(!miners.contains(miner))
      require(!active.contains(miner))
    case e : RemotingLifecycleEvent =>
      log.info("remoting lifecycle {}", e)
    case Master.Ready(miner) =>
      log.debug("watch {}", miner)
      watch(miner)
      miners += miner
      dispatch(sender)
    case Master.NewWork(work) =>
      work match {
        case Some(getWork) =>
          log.info("load {}", getWork.blockHeader)
          proofOfWork = Some(getWork.proofOfWork)
          nonce = 0
          for (miner <- miners &~ active)
            dispatch(miner)
        case None =>
      }
      if (config.refresh >= 1.seconds)
        load(config.refresh)
    case msg @ Master.BlockFound(blockHeader, startNonce, checked,
        startTime, elapsed) =>
      proofOfWork.get.stopWatch.stop
      val hashRate = config.unit.toDouble / elapsed.toNanos * 1000000000
      val now = System.nanoTime()
      val deliveryTime = (now - startTime - elapsed.toNanos).nanos
      log.debug("checked #{} in {}ms, {} khash/s",
        startNonce.toLong / config.unit, elapsed.toMillis, hashRate / 1000)
      metrics.hashRate(hashRate.toLong)
      metrics.hashTime(elapsed.toMicros)
      metrics.deliveryTime(deliveryTime.toMicros)
      blockHeader match {
        case Some(found) =>
          submit(found, (msg, sender))
          load()
        case None =>
          dispatch(sender)
      }
    case Master.Accepted(found, (msg, _)) =>
      val stopWatch = proofOfWork.get.stopWatch
      proofOfWork = None
      log.info("ACCEPTED {} in {}s {} khash/s",
        found,
        stopWatch.getElapsedTime.toDouble / 1000,
        found.nonce.toLong.toDouble / stopWatch.getElapsedTime)
      if (probe != null)
        probe ! msg
      metrics.accepted()
    case Master.Rejected(found, _) =>
      log.info("REJECTED {}", found)
      metrics.rejected()
  }
}
