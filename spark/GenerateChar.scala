package com.wttttt.spark

import java.io.PrintWriter
import java.net.ServerSocket

/**
  * Created with IntelliJ IDEA.
  * Description: 
  * Author: wttttt
  * Github: https://github.com/wttttt-wang/hadoop_inaction
  * Date: 2017-05-19
  * Time: 10:19
  */
object GenerateChar {
  def main(args: Array[String]) {
    val listener = new ServerSocket(9998)
    while(true){
      val socket = listener.accept()
      new Thread(){
        override def run() = {
          println("Got client connected from :"+ socket.getInetAddress)
          val out = new PrintWriter(socket.getOutputStream,true)
          while(true){
            Thread.sleep(3000)
            val context1 = "GET /result.html?Input=test1 HTTP/1.1"
            println(context1)
            val context2 = "GET /result.html?Input=test2 HTTP/1.1"
            println(context2)
            val context3 = "GET /result.html?Input=test3 HTTP/1.1"
            println(context3)
            out.write(context1 + '\n' + context2 + "\n" + context2 + "\n" + context3 + "\n" + context3 + "\n" + context3 + "\n" + context3 + "\n")
            out.flush()
          }
          socket.close()
        }
      }.start()
    }
  }
}
