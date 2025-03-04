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

package io.gatling.redis.feeder

import io.gatling.core.feeder.Record
import io.gatling.redis.Predef._

import com.redis._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

class RedisFeederSpec extends AnyFlatSpecLike with Matchers with MockitoSugar {
  val KEY = "key"
  val KEYDEST = "key2"

  // Generate list of maps Map(<redis-key> -> <expected-value>)
  def valsLst(key: String, s: String*): List[Record[String]] =
    s.map(str => Map(key -> str)).toList

  trait MockContext {
    var clientPool: RedisClientPool = mock[RedisClientPool]
    var client: RedisClient = mock[RedisClient]

    // Call user specified function on withClient() call
    when(clientPool.withClient(any())).thenAnswer { invocation =>
      val arguments = invocation.getArguments
      val func = arguments(0).asInstanceOf[Function[RedisClient, AnyRef]]
      func(client)
    }
  }

  "redis feeder" should "use lpop as default command" in {
    new MockContext {
      when(client.lpop(KEY)).thenReturn(Some("v1"), Some("v2"), Some("v3"), None)
      redisFeeder(clientPool, KEY).apply().toList shouldBe valsLst(KEY, "v1", "v2", "v3")
    }
  }

  it should "use spop command" in {
    new MockContext {
      when(client.spop(KEY)).thenReturn(Some("v1"), Some("v2"), Some("v3"), None)
      redisFeeder(clientPool, KEY).SPOP.apply().toList shouldBe valsLst(KEY, "v1", "v2", "v3")
    }
  }

  it should "use srandmember command" in {
    new MockContext {
      when(client.srandmember(KEY)).thenReturn(Some("v1"), Some("v2"), Some("v3"))

      val feeder = redisFeeder(clientPool, KEY).SRANDMEMBER.apply()

      feeder.next() shouldBe Map(KEY -> "v1")
      feeder.next() shouldBe Map(KEY -> "v2")
      feeder.next() shouldBe Map(KEY -> "v3")
    }
  }

  it should "use rpoplpush command" in {
    new MockContext {
      when(client.rpoplpush(KEY, KEYDEST)).thenReturn(Some("v3"), Some("v2"), Some("v1"), None)

      redisFeeder(clientPool, KEY, KEYDEST).RPOPLPUSH.apply().toList shouldBe valsLst(KEY, "v3", "v2", "v1")
    }
  }
}
