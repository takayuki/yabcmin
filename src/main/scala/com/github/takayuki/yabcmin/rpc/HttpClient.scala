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
import com.google.inject.Inject
import java.net.URL
import org.slf4j.LoggerFactory
import scala.util.parsing.json.JSONObject
import uk.co.bigbeeconsultants.http.Config
import uk.co.bigbeeconsultants.http.auth.Credential
import uk.co.bigbeeconsultants.http.header.Headers
import uk.co.bigbeeconsultants.http.header.MediaType.APPLICATION_JSON
import uk.co.bigbeeconsultants.http.request.RequestBody
import uk.co.bigbeeconsultants.http.response.Response

object HttpClient {
  case class Auth(url : String, user : String, password : String)
}

class HttpClient extends ClientInterface {

  private val log = LoggerFactory.getLogger(classOf[HttpClient])

  @Inject
  val auth : HttpClient.Auth = null

  @Inject
  val config : Config = null

  private[this] def newClient =
    new uk.co.bigbeeconsultants.http.HttpClient(config)

  private def post(json : JSONObject): Response = {
    val client = newClient
    val url = new URL(auth.url)
    val credential = new Credential(auth.user, auth.password)
    val header = Headers(credential.toBasicAuthHeader)
    val body = RequestBody(json.toString(), APPLICATION_JSON)
    val response = client.post(url, Some(body), header)
    response
  }

  override def getwork(): Option[GetWork] = {

    try {
      val response = post(GetWork.toJson)
      response.status.code match {
        case 200 =>
          val json = response.body.asString
          val result = GetWork.fromJson(json)
          result match {
            case Some(work) =>
              log.debug("got {})", work.blockHeader)
            case None =>
              log.error("invalid format in {})", json)
          }
          result
        case _ =>
          log.debug("no block found in {})", response)
          None
      }
    } catch {
      case e : Throwable =>
        log.error("while getting a work", e)
        None
    }
  }

  override def getwork(blockHeader: BlockHeader) : Boolean = {

    try {
      val response = post(GetWork.toJson(blockHeader))
      response.status.code match {
        case 200 =>
          log.debug("accepted {}, {}, {})",
            blockHeader, response.status, response.body)
          true
        case _ =>
          log.debug("rejected {}, {}, {})",
            blockHeader, response.status, response.body)
          false
      }
    } catch {
      case e : Throwable =>
        log.error("while submitting a work", e)
        false
    }
  }

}
