package com.wttttt.hadoop;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Author: wttttt
 * Github: https://github.com/wttttt-wang/hadoop_inaction
 * Date: 2017-06-07
 * Time: 11:02
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;


public class GetNgramToFile {

    private static void getTopK(Iterable<Text> values, Queue<String> heap, int topNum){
        for (Text value : values){
            if (heap.size() < topNum) {
                heap.offer(value.toString());
            } else{
                heap.offer(value.toString());
                heap.poll();
            }
        }
    }

    public static class CountMapper extends
            org.apache.hadoop.mapreduce.Mapper<LongWritable, Text, Text, Text> {
        private int threshold;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            threshold = context.getConfiguration().getInt("threshold", 10);
        }

        /**
         * Description: filter the record whose count < threshold
         * */
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: v1 v2 ... vi\tcount
            String[] line = value.toString().trim().split("\t");
            String[] words = line[0].split("\\s+");
            int count = Integer.parseInt(line[1]);

            if (count < threshold || words.length < 2) return;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < words.length - 1; i++) {
                sb.append(words[i]);
                sb.append(" ");
            }

            context.write(new Text(sb.toString().trim()), new Text(words[words.length - 1]
                    + "=" + count));
            // output: key = v1 v2 ... vi-1  value = vi=count
        }
    }



    public static class CountReducer extends Reducer<Text, Text, Text, Text> {

        private int topNum;
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            topNum = context.getConfiguration().getInt("topNum", 5);
        }

        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // input:  key = v1 v2 ... vi-1   value = vi=count

            Queue<String> heap = new PriorityQueue<String>(topNum, ValueComparator);

            getTopK(values, heap, topNum);

            // output: key = v1 v2 ... vi-1   value = vi=count
            int size = heap.size();
            for (int i = 0; i < size; i++) {
                context.write(key, new Text(heap.poll()));
            }
        }

    }


    public static Comparator<String> ValueComparator = new Comparator<String>() {
        public int compare(String s1, String s2) {
            return Integer.parseInt(s1.split("=")[1].trim()) - Integer.parseInt(s2.split("=")[1].trim());
        }
    };


    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException{
        // 1. command line parser
        String inputPath = args[0];
        String outputPath = args[1];
        int threshold = Integer.parseInt(args[2]);
        int topNum = Integer.parseInt(args[3]);


        Configuration conf2 = new Configuration();
        conf2.setInt("threshold", threshold);
        conf2.setInt("topNum", topNum);


        Job job2 = Job.getInstance(conf2, "GetNgram");
        job2.setJarByClass(GetNgramToFile.class);

        job2.setMapperClass(GetNgramToFile.CountMapper.class);
        job2.setReducerClass(GetNgramToFile.CountReducer.class);

        // combiner -->
        // same with reducer only when mapper and reducer are of same <key, value> type
        job2.setCombinerClass(GetNgram.CountReducer.class);


        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(Text.class);

        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);

        TextInputFormat.setInputPaths(job2, new Path(inputPath));
        TextOutputFormat.setOutputPath(job2, new Path(outputPath));
        job2.waitForCompletion(true);

    }

}

