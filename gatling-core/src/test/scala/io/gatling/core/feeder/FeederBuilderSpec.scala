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

package io.gatling.core.feeder

import java.io.FileNotFoundException

import scala.collection.immutable

import io.gatling.core.config._

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class FeederBuilderSpec extends AnyFlatSpecLike with Matchers with FeederSupport {
  private implicit val configuration: GatlingConfiguration = GatlingConfiguration.loadForTest()

  "FeederSupport.separatedValues" should "throw an exception when provided with bad resource" in {
    an[FileNotFoundException] should be thrownBy
      separatedValues("fileDoesNotExist", SeparatedValuesParser.CommaSeparator, quoteChar = '\'')
  }

  "FeederSupport.seq2FeederBuilder" should "build a Feeder with a queue strategy" in {
    val queuedFeeder = IndexedSeq(Map("1" -> "Test"), Map("2" -> "Test")).queue.apply()
    queuedFeeder.toArray shouldBe Array(Map("1" -> "Test"), Map("2" -> "Test"))
  }

  it should "build a Feeder with a random strategy" in {
    val fiftyTimes = 1 to 50
    val orderedMaps =
      fiftyTimes.foldLeft(IndexedSeq.empty[Record[String]]) { (acc, id) =>
        Map(id.toString -> "Test") +: acc
      }

    val testsOutcome: immutable.IndexedSeq[Boolean] =
      (1 to 3).map { _ =>
        val randomFeeder = orderedMaps.random.apply()
        randomFeeder.hasNext shouldBe true
        val retrievedMaps = fiftyTimes.map(_ => randomFeeder.next())
        retrievedMaps != orderedMaps
      }

    if (!testsOutcome.reduce(_ || _)) fail("Random feeder did not return a random order even once out of three attempts")
  }

  it should "build a Feeder with a shuffle strategy" in {
    val fiftyTimes = 1 to 50
    val orderedMaps =
      fiftyTimes.foldLeft(IndexedSeq.empty[Record[String]]) { (acc, id) =>
        Map(id.toString -> "Test") +: acc
      }

    val shuffledOutcome: immutable.IndexedSeq[IndexedSeq[Record[_]]] =
      (1 to 3).map { _ =>
        val shuffleFeeder = orderedMaps.shuffle.apply()
        shuffleFeeder.hasNext shouldBe true
        fiftyTimes.map(_ => shuffleFeeder.next())
      }

    val allShuffledSeqsAreDifferent = (shuffledOutcome :+ orderedMaps).distinct.sizeIs == 4
    if (!allShuffledSeqsAreDifferent) fail("Shuffle feeder returned the same order at least once out of three attempts")
  }

  it should "build a Feeder with a circular strategy" in {
    val circularFeeder = IndexedSeq(Map("1" -> "Test"), Map("2" -> "Test")).circular.apply()
    circularFeeder.next()
    circularFeeder.next()
    circularFeeder.next() shouldBe Map("1" -> "Test")
  }

  "RecordSeqFeederBuilder" should "be able to have a record transformed" in {
    val queuedFeeder = IndexedSeq(Map("1" -> "Test"), Map("2" -> "Test"))

    val transformedValue: Option[Any] = queuedFeeder
      .transform { case ("1", attr) =>
        attr.concat("s are boring !")
      }
      .apply()
      .next()
      .get("1")

    transformedValue.fold(fail("Could not find key"))(_ shouldBe "Tests are boring !")

    val cantTransform: Option[Any] = queuedFeeder
      .transform { case ("Can't find because don't exist", shouldKeepAsIs) =>
        shouldKeepAsIs.concat("s are boring !")
      }
      .apply()
      .next()
      .get("1")

    cantTransform.fold(fail("Could not find key"))(_ shouldBe "Test")
  }
}
