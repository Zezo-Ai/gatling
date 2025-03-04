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

package io.gatling.commons.util

import java.nio.charset.StandardCharsets.UTF_8

import scala.util.Using

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class CompositeByteArrayInputStreamSpec extends AnyFlatSpecLike with Matchers {
  "CompositeByteArrayInputStream" should "properly read full bytes" in {
    val bytes1 = "hello".getBytes(UTF_8)
    val bytes2 = " ".getBytes(UTF_8)
    val bytes3 = "world".getBytes(UTF_8)
    val concat = Using.resource(new CompositeByteArrayInputStream(Seq(bytes1, bytes2, bytes3))) { is =>
      new String(is.readAllBytes(), UTF_8)
    }

    concat shouldBe "hello world"
  }

  it should "throw when a chunk is empty" in {
    a[IllegalArgumentException] shouldBe thrownBy(new CompositeByteArrayInputStream(Seq("".getBytes(UTF_8))))
  }
}
