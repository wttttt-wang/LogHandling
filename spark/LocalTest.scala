package com.wttttt.spark

/**
  * Created with IntelliJ IDEA.
  * Description: 
  * Author: wttttt
  * Github: https://github.com/wttttt-wang/hadoop_inaction
  * Date: 2017-05-19
  * Time: 09:56
  */
import java.util.regex.Pattern

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Milliseconds, StreamingContext}
import org.slf4j.LoggerFactory

import scala.collection.mutable

object LocalTest {
  val logger = LoggerFactory.getLogger("LocalTest")
  def main(args: Array[String]) {

    val batchInterval = Milliseconds(10000)
    val slideInterval = Milliseconds(5000)

    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("LocalTest")
    // WARN StreamingContext: spark.master should be set as local[n], n > 1 in local mode if you have receivers to get data,
    // otherwise Spark jobs will not get resources to process the received data.
    val sc = new StreamingContext(conf, Milliseconds(5000))
    sc.checkpoint("flumeCheckpoint/")

    val stream = sc.socketTextStream("localhost", 9998)

    val counts = stream.mapPartitions{ events =>
      val pattern = Pattern.compile("\\?Input=[^\\s]*\\s")
      val map = new mutable.HashMap[String, Int]()
      logger.info("Handling events, events is empty: " + events.isEmpty)
      while (events.hasNext){   // par is an Iterator!!!
      val line = events.next()
        val m = pattern.matcher(line)
        if (m.find()) {
          val words = line.substring(m.start(), m.end()).split("=")(1).toLowerCase()
          logger.info(s"Processing words $words")
          map.put(words, map.getOrElse(words, 0) + 1)
        }
      }
      map.iterator
    }

    val window = counts.reduceByKeyAndWindow(_+_, _-_, batchInterval, slideInterval)
    // window.print()

    // transform和它的变体trnasformWith运行在DStream上任意的RDD-to-RDD函数;
    // 可以用来使用那些不包含在DStrema API中RDD操作
    val sorted = window.transform(rdd =>{
      val sortRdd = rdd.map(t => (t._2, t._1)).sortByKey(false).map(t => (t._2, t._1))
      val more = sortRdd.take(2)
      more.foreach(println)
      sortRdd
    })

    sorted.print()

    sc.start()
    sc.awaitTermination()
  }
}
