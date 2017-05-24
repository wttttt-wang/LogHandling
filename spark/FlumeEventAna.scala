package com.wttttt.spark

import java.util.regex.Pattern

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.slf4j.LoggerFactory

import scala.collection.mutable

object FlumeEventAna {
  val logger = LoggerFactory.getLogger("FlumeEventAna")
  def main(args: Array[String]) {
    if (args.length < 5) {
      System.err.println(
        "Usage: FlumeEventCount <host> <port> <windowSize> <numBatch> <numSlide>")
      System.exit(1)
    }

    //StreamingExamples.setStreamingLogLevels()

    val host = args(0)
    val port = args(1).toInt
    val windowSize = Milliseconds(args(2).toInt)
    val batchInterval = Milliseconds(args(2).toInt * args(3).toInt)
    val slideInterval = Milliseconds(args(2).toInt * args(4).toInt)

    logger.debug(s"Listening on $host at $port batching records every $batchInterval")

    // Create the context and set the batch size
    val sparkConf = new SparkConf().setAppName("FlumeEventAna")
    val ssc = new StreamingContext(sparkConf, windowSize)

    ssc.checkpoint("hdfs://host99:9000/flumeCheckpoint")

    // accumulator for debugging
    val accum = ssc.sparkContext.longAccumulator("Search words Accumulator")

    val accum1 = ssc.sparkContext.longAccumulator("Events Accumulator")

    // Create a flume stream  type of ReceiverInputDStream
    val stream = FlumeUtils.createStream(ssc, host, port, StorageLevel.MEMORY_ONLY_SER_2)


    val counts = stream.mapPartitions{ events =>
      val pattern = Pattern.compile("\\?Input=[^\\s]*\\s")
      val map = new mutable.HashMap[String, Int]()
      logger.info("Handling events, events is empty: " + events.isEmpty)
      while (events.hasNext){   // par is an Iterator!!!
        val line = events.next().event.getBody.toString
        System.out.println("handling line: " + line)

        val m = pattern.matcher(line)
        if (m.find()) {
          val words = line.substring(m.start(), m.end()).split("=")(1).toLowerCase()
          System.out.println("search word found: " + words)
          logger.info(s"Processing words $words")
          map.put(words, map.getOrElse(words, 0) + 1)
          accum.add(1)
        }
      }
      map.iterator
    }

    val window = counts.reduceByKeyAndWindow(_+_, _-_, batchInterval, slideInterval)

    val sorted = window.transform(rdd =>{
      val sortRdd = rdd.map(t => (t._2, t._1)).sortByKey(false).map(t => (t._2, t._1))
      val more = sortRdd.take(5)
      more.foreach(println)
      sortRdd
    })

    // sorted.print()
    sorted.saveAsTextFiles("output/")

    ssc.start()
    ssc.awaitTermination()
  }
}