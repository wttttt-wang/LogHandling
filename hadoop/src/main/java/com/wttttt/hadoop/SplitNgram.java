package com.wttttt.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitNgram {
    public static class SplitMapper extends Mapper<LongWritable, Text, Text, IntWritable>{

        int noGram;
        Pattern pattern = Pattern.compile("\\?input=[^\\x22]*");

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            noGram = conf.getInt("NoGram", 3);
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // value: 10.109.255.90 - - [12/May/2017:14:31:46 +0800] "GET /result.html?Input=Mickey HTTP/1.1" 304  "http://10.3.242.101/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/602.4.8 (KHTML, like Gecko) Version/10.0.3 Safari/602.4.8"
            // 1. regrex matching
            String line = value.toString().trim();
            System.out.println("Handling line: " + line);
            Matcher m = pattern.matcher(line);
            if (m.find()){
                String input = line.substring(m.start(), m.end());
                String searchWords = input.split("=")[1].toLowerCase().replace("\\x22", "").
                        replaceAll("[^a-z\\+\\s]", " ").trim();

                String[] words = searchWords.split("\\s+");
                if (words.length < 2) return;

                StringBuilder sb;
                for (int i = 0; i < words.length; i++) {
                    sb = new StringBuilder();
                    sb.append(words[i]);
                    for (int j = 1; j < noGram && i + j < words.length; j++) {
                        sb.append(" ");
                        sb.append(words[i + j]);
                        // output: (This is), (is my)... (This is my)
                        context.write(new Text(sb.toString().trim()), new IntWritable(1));
                    }
                }
            }
        }
    }


    public static class SplitReducer extends Reducer<Text, IntWritable, Text, IntWritable>{
        /**
         * Description: simply count the occurance times for ngram
         * output: key --> ngram(i connective words, 2 <= i <= n)   value --> count
         */
        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException{
        if (args.length < 3){
            System.err.println("Usage: com.wttttt.hadoop.SplitNgram <input> <output> <noGram>");
            System.exit(2);
        }

        // 1. command line parser
        String inputPath = args[0];
        String outputPath = args[1];
        String noGram = args[2];

        Configuration conf = new Configuration();
        conf.set("noGram", noGram);

        Job job1 = Job.getInstance(conf, "SplitNgram");
        job1.setJarByClass(SplitNgram.class);

        job1.setMapperClass(SplitMapper.class);
        job1.setReducerClass(SplitReducer.class);
        // combiner -->
        // same with reducer only when mapper and reducer are of same <key, value> type
        job1.setCombinerClass(SplitReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);

        job1.setInputFormatClass(TextInputFormat.class);
        // job1.setInputFormatClass(PatternInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);

        TextInputFormat.setInputPaths(job1, new Path(inputPath));
        TextOutputFormat.setOutputPath(job1, new Path(outputPath));
        job1.waitForCompletion(true);

    }

}
