package com.wttttt.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.db.DBConfiguration;
import org.apache.hadoop.mapreduce.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * Author: wttttt
 * Github: https://github.com/wttttt-wang/hadoop_inaction
 * Date: 2017-04-18
 * Time: 16:28
 */
public class Driver {
    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException{
        if (args.length < 6) {
            System.err.println("Usage: Driver <in1> <out1> <out2> <noGram> <threshold> <topNum> <mysqlConf> <user> <pwd>");
            System.exit(2);
        }

        String inputPath1 = args[0];
        String outputPath1 = args[1];
        String outputPath2 = args[2];
        int noGram = Integer.parseInt(args[3]);
        int threshold = Integer.parseInt(args[4]);
        int topNum = Integer.parseInt(args[5]);

        String mysqlConf = "jdbc:mysql://10.3.242.99:3306/QuoraLogs";
        String user = "root";
        String pwd = "111111";
        if (args.length > 6){
            mysqlConf = args[4];
            user = args[5];
            pwd = args[6];
        }

        Configuration conf1 = new Configuration();
        conf1.setInt("noGram", noGram);

        Job job1 = Job.getInstance(conf1, "SplitNgram");
        job1.setJarByClass(SplitNgram.class);

        job1.setMapperClass(SplitNgram.SplitMapper.class);
        job1.setReducerClass(SplitNgram.SplitReducer.class);

        // set combiner
        job1.setCombinerClass(SplitNgram.SplitReducer.class);

        // mapper and reducer share the same key&value type
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);

        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);

        TextInputFormat.setInputPaths(job1, new Path(inputPath1));
        TextOutputFormat.setOutputPath(job1, new Path(outputPath1));

        job1.waitForCompletion(true);

        // job2

        Configuration conf2 = new Configuration();
        conf2.setInt("threshold", threshold);
        conf2.setInt("topNum", topNum);

        DBConfiguration.configureDB(conf2, "com.mysql.jdbc.Driver", mysqlConf, user, pwd);

        Job job2 = Job.getInstance(conf2, "GetNgram");
        job2.setJarByClass(GetNgram.class);

        // add third-party jar to classpath
        job2.addArchiveToClassPath(new Path("hdfs://10.3.242.99:9000/lib/mysql-connector-java-5.1.15-bin.jar"));

        job2.setMapperClass(GetNgram.CountMapper.class);
        job2.setReducerClass(GetNgram.DBReducer.class);

        // this combiner is not the same with reducer
        job2.setCombinerClass(GetNgram.CountReducer.class);

        // mapper and reducer are of different type, so specify each
        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setOutputKeyClass(DBOutputWritable.class);
        job2.setOutputValueClass(NullWritable.class);

        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(DBOutputFormat.class);
        //(.., tableName, FiledNames)
        DBOutputFormat.setOutput(job2, "output", "origin", "predict", "count");

        TextInputFormat.setInputPaths(job2, new Path(outputPath1));
        TextOutputFormat.setOutputPath(job2, new Path(outputPath2));
        job2.waitForCompletion(true);
    }
}