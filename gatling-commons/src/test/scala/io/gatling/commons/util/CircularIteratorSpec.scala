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

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class CircularIteratorSpec extends AnyFlatSpecLike with Matchers {
  "CircularIterator" should "work fine with non empty Iterable with threadsafe on" in {
    val rr = CircularIterator(IndexedSeq(1, 2, 3), threadSafe = true)

    rr.next() shouldBe 1
    rr.next() shouldBe 2
    rr.next() shouldBe 3
    rr.next() shouldBe 1
    rr.next() shouldBe 2
    rr.next() shouldBe 3
  }

  it should "work fine with non empty Iterable with threadsafe off" in {
    val rr = CircularIterator(IndexedSeq(1, 2, 3), threadSafe = false)

    rr.next() shouldBe 1
    rr.next() shouldBe 2
    rr.next() shouldBe 3
    rr.next() shouldBe 1
    rr.next() shouldBe 2
    rr.next() shouldBe 3
  }

  it should "always return the same value when iterating a single value Iterable" in {
    val rr = CircularIterator(IndexedSeq(1), threadSafe = false)

    rr.next() shouldBe 1
    rr.next() shouldBe 1
    rr.next() shouldBe 1
    rr.next() shouldBe 1
    rr.next() shouldBe 1
    rr.next() shouldBe 1
  }

  it should "throw NoSuchElementException when iterating on an empty Iterable" in {
    val rr = CircularIterator(IndexedSeq.empty[Int], threadSafe = false)

    a[NoSuchElementException] should be thrownBy rr.next()
  }
}
