package com.wttttt.spark

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.flume.FlumeUtils
import org.apache.spark.streaming.{Milliseconds, StreamingContext}

object FlumeEventCount {
  def main(args: Array[String]) {
    if (args.length < 3) {
      System.err.println(
        "Usage: FlumeEventCount <host> <port> <BatchInterval>")
      System.exit(1)
    }

    //StreamingExamples.setStreamingLogLevels()

    val host = args(0)
    val port = args(1).toInt
    val batchInterval = Milliseconds(args(3).toInt)

    // Create the context and set the batch size
    val sparkConf = new SparkConf().setAppName("FlumeEventCount")
    val ssc = new StreamingContext(sparkConf, batchInterval)

    // Create a flume stream
    val stream = FlumeUtils.createStream(ssc, host, port, StorageLevel.MEMORY_ONLY_SER_2)

    // Print out the count of events received from this server in each batch
    stream.count().map(cnt => "Received " + cnt + " flume events." ).print()

    ssc.start()
    ssc.awaitTermination()
  }
}
// scalastyle:on println

