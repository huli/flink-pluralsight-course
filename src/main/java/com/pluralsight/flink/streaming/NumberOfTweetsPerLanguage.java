package com.pluralsight.flink.streaming;

import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.util.Collector;
import org.apache.jute.compiler.JString;

import java.util.Date;
import java.util.Properties;

public class NumberOfTweetsPerLanguage {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties props = TwitterConfiguration.getProperties();

        env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);

        env.addSource(new TwitterSource(props))
                .map(new MapToTweet())
                .keyBy(new KeySelector<Tweet, String>() {
                    @Override
                    public String getKey(Tweet value) throws Exception {
                        return value.getLanguage();
                    }
                })
                .timeWindow(Time.seconds(30))
                .apply(new WindowFunction<Tweet, Tuple3<String, Long, Date>, String, TimeWindow>() {
                    @Override
                    public void apply(String language, TimeWindow window,
                                      Iterable<Tweet> iterable,
                                      Collector<Tuple3<String, Long, Date>> collector) throws Exception {

                        long count = 0;
                        for(Tweet tweet : iterable){
                            count++;
                        }

                        collector.collect(new Tuple3<>(language, count, new Date(window.getEnd())));
                    }
                })
                .print();

        env.execute();
    }
}
