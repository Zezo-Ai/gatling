/*
 * Copyright 2011-2025 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.recorder.http.mitm.controller

import io.gatling.commons.util.Clock
import io.gatling.recorder.http.{ OutgoingProxy, Remote }
import io.gatling.recorder.http.mitm.TrafficLogger
import io.gatling.recorder.http.mitm.controller.Netty._
import io.gatling.recorder.util.HttpUtils

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.handler.codec.http._

private[controller] final class PlainWithProxyController(
    serverChannel: Channel,
    clientBootstrap: Bootstrap,
    proxy: OutgoingProxy,
    trafficLogger: TrafficLogger,
    clock: Clock
) extends PlainController(serverChannel, clientBootstrap, trafficLogger, clock) {
  private val proxyRemote = Remote(proxy.host, proxy.port)
  private val proxyBasicAuthHeader = proxy.credentials.map(HttpUtils.basicAuth)

  override protected def connectedRemote(requestRemote: Remote): Remote = proxyRemote

  override protected def propagatedRequest(originalRequest: FullHttpRequest): FullHttpRequest =
    (proxyBasicAuthHeader match {
      case Some(header) =>
        val requestWithCreds = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, originalRequest.method, originalRequest.uri, originalRequest.content)
        requestWithCreds.headers
          .set(originalRequest.headers)
          .set(HttpHeaderNames.PROXY_AUTHORIZATION, header)

        requestWithCreds

      case _ => originalRequest
    }).filterSupportedEncodings
}
