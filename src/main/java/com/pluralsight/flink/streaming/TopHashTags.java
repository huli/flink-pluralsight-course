package com.pluralsight.flink.streaming;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.AllWindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.util.Collector;

import java.util.Date;
import java.util.Properties;

public class TopHashTags {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties props = TwitterConfiguration.getProperties();
        
        env.addSource(new TwitterSource(props))
                .map(new MapToTweet())
                .flatMap(new FlatMapFunction<Tweet, Tuple2<String, Integer>>() {
                    @Override
                    public void flatMap(Tweet value,
                                        Collector<Tuple2<String, Integer>> collector) throws Exception {
                        for(String tag : value.getTags()){
                            collector.collect(new Tuple2<>(tag, 1));
                        }
                    }
                })
                .keyBy(0)
                .timeWindow(Time.seconds(30))
                .sum(1)
                .timeWindowAll(Time.seconds(30))
                .apply(new AllWindowFunction<Tuple2<String, Integer>, Tuple3<Date, String, Integer>, TimeWindow>() {
                    @Override
                    public void apply(TimeWindow window,
                                      Iterable<Tuple2<String, Integer>> iterator,
                                      Collector<Tuple3<Date, String, Integer>> collector) throws Exception {

                        String topTag = null;
                        Integer count = 0;
                        for(Tuple2<String, Integer> hashtag : iterator){
                            if(hashtag.f1 > count){
                                topTag = hashtag.f0;
                                count = hashtag.f1;
                            }
                        }

                        collector.collect(new Tuple3<>(new Date(window.getEnd()), topTag, count));
                    }
                })
                .print();

        env.execute();
    }
}
