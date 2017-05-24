package com.wttttt.spark

import java.util.regex.Pattern

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.slf4j.LoggerFactory

import scala.collection.mutable
import java.util.Date

/**
  * Created with IntelliJ IDEA.
  * Description: 
  * Author: wttttt
  * Github: https://github.com/wttttt-wang/hadoop_inaction
  * Date: 2017-05-24
  * Time: 14:40
  */
object SocketEventTest {
  val logger = LoggerFactory.getLogger("LocalTest")
  def main(args: Array[String]) {

    if (args.length < 5) {
      System.err.println(
        "Usage: SocketEvent <host> <port> <windowSize> <numBatch> <numSlide>")
      System.exit(1)
    }


    val host = args(0)
    val port = args(1).toInt
    val windowSize = Milliseconds(args(2).toInt)
    val batchInterval = Milliseconds(args(2).toInt * args(3).toInt)
    val slideInterval = Milliseconds(args(2).toInt * args(4).toInt)


    val conf = new SparkConf()
      .setAppName("SocketEventTest")

    val sc = new StreamingContext(conf, windowSize)
    sc.checkpoint("flumeCheckpoint/")

    val stream = sc.socketTextStream(host, port)

    val counts = stream.mapPartitions { events =>
      val pattern = Pattern.compile("\\?Input=[^\\s]*\\s")
      val map = new mutable.HashMap[String, Int]()
      while (events.hasNext) {
        val line = events.next()
        System.out.println(s"Handling line $line")
        val m = pattern.matcher(line)
        if (m.find()) {
          val words = line.substring(m.start(), m.end()).split("=")(1).toLowerCase()
          System.out.println(s"Search word found: $words")
          logger.info(s"Processing words $words")
          map.put(words, map.getOrElse(words, 0) + 1)
        }
      }
      map.iterator
    }

    val window = counts.reduceByKeyAndWindow(_ + _, _ - _, batchInterval, slideInterval)

    val sorted = window.transform(rdd => {
      val sortRdd = rdd.map(t => (t._2, t._1)).sortByKey(false).map(t => (t._2, t._1))
      // val more = sortRdd.take(2)
      // more.foreach(a => println(s"TopK : $a"))
      sortRdd
    })




    val date = new Date()
    val time = date.getTime

    sorted.saveAsTextFiles(s"/staticLog/socketOutput/$time")

    sc.start()
    sc.awaitTermination()
  }

}
