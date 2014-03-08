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

import akka.actor._
import com.codahale.metrics.{JmxReporter, MetricRegistry}
import com.github.takayuki.yabcmin.miner.{Loader, Miner, Slave, Master}
import com.github.takayuki.yabcmin.rpc.{ClientInterface, HttpClient}
import com.google.inject.name.Names
import com.google.inject.util.Providers
import com.google.inject.{AbstractModule, Guice, Injector}
import org.apache.commons.cli.{HelpFormatter, ParseException, GnuParser, Options}
import scala.concurrent.duration._
import uk.co.bigbeeconsultants.http.{ Config => HttpConfig }

object App {

  import com.github.takayuki.yabcmin.Mode._

  class Config {
    var url = "http://127.0.0.1:8332/"
    var user = "bitcoinrpc"
    var pass = "********"
    var miners = 1
    var unit = 1000000L
    var refresh = 60000
    var connectTimeout = 5000
    var readTimeout = 5000
    var mode = STANDALONE
    var masterPath = ".."
    var receiveTimeout = 20000
  }

  def options = {
    val opts = new Options()
    opts.addOption("B", "server", true, "Bitcoin RPC server URL")
    opts.addOption("U", "user", true, "username")
    opts.addOption("P", "pass", true, "password")
    opts.addOption("n", "miners", true, "number of miners")
    opts.addOption("u", "unit", true, "unit size")
    opts.addOption("r", "refresh", true, "refresh in ms")
    opts.addOption("C", "connect-timeout", true, "connect timeout in ms [http]")
    opts.addOption("R", "read-timeout", true, "read timeout in ms [http]")
    opts.addOption("l", "master", true, "master path (slave mode)")
    opts.addOption("t", "receive-timeout", true, "receive timeout in ms [remoting]")
    opts.addOption("h", "help", false, "show this message")
    opts
  }

  def main(args : Array[String]) {
    val config = new Config
    val opts = options
    def help(code : Int) = {
      val formatter = new HelpFormatter()
      formatter.printHelp("yabcmin", opts)
      System.exit(code)
    }
    val parser = new GnuParser()
    try {
      val cmd = parser.parse(opts, args)
      if (cmd.hasOption('B'))
        config.url = cmd.getOptionValue('B')
      if (cmd.hasOption('U'))
        config.user = cmd.getOptionValue('U')
      if (cmd.hasOption('P'))
        config.pass = cmd.getOptionValue('P')
      if (cmd.hasOption('n'))
        config.miners = Integer.parseInt(cmd.getOptionValue('n'))
      if (cmd.hasOption('u'))
        config.unit= java.lang.Long.parseLong(cmd.getOptionValue('u'))
      if (cmd.hasOption('r'))
        config.refresh = Integer.parseInt(cmd.getOptionValue('r'))
      if (cmd.hasOption('C'))
        config.connectTimeout = Integer.parseInt(cmd.getOptionValue('C'))
      if (cmd.hasOption('R'))
        config.readTimeout = Integer.parseInt(cmd.getOptionValue('R'))
      if (cmd.hasOption('l')) {
        config.masterPath = cmd.getOptionValue('l')
        config.mode = SLAVE
      }
      if (cmd.hasOption('t'))
        config.receiveTimeout = Integer.parseInt(cmd.getOptionValue('t'))
      if (cmd.hasOption('h'))
        help(0)
    } catch {
      case e : ParseException =>
        help(1)
    }

    val registry = new MetricRegistry()
    val reporter = JmxReporter.forRegistry(registry).build()
    reporter.start()
    val system = ActorSystem("yabcmin")
    val injector : Injector = Guice.createInjector(new AbstractModule {
      override def configure(): Unit = {
        val auth = HttpClient.Auth(
          config.url,
          config.user,
          config.pass)
        val httpConfig = new HttpConfig(
          connectTimeout = config.connectTimeout,
          readTimeout = config.readTimeout)
        val masterConfig = new Master.Config(
          config.miners,
          config.unit,
          config.refresh.millis)
        val slaveConfig = new Slave.Config(
          config.miners)
        val minerConfig = new Miner.Config(
          config.masterPath,
          config.receiveTimeout.millis)
        bind(classOf[HttpClient.Auth]).toInstance(auth)
        bind(classOf[HttpConfig]).toInstance(httpConfig)
        bind(classOf[Master.Config]).toInstance(masterConfig)
        bind(classOf[Slave.Config]).toInstance(slaveConfig)
        bind(classOf[Miner.Config]).toInstance(minerConfig)
        bind(classOf[ClientInterface]).to(classOf[HttpClient])
        bind(classOf[ActorRef])
          .annotatedWith(Names.named("MasterMinerProbe"))
          .toProvider(Providers.of(null))
        bind(classOf[MetricRegistry]).toInstance(registry)
      }})
    config.mode match {
      case STANDALONE =>
        system.actorOf(Loader.props(injector), "loader")
      case SLAVE =>
        system.actorOf(Slave.props(injector), "slave")
    }
  }
}
