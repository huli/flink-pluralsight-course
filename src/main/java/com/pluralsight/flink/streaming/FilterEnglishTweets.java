package com.pluralsight.flink.streaming;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;

import java.util.Properties;

public class FilterEnglishTweets {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties props = TwitterConfiguration.getProperties();

        env.addSource(new TwitterSource(props))
                .map(new MapToTweet())
                .filter(new FilterFunction<Tweet>() {
                    @Override
                    public boolean filter(Tweet value) throws Exception {
                        return value.getLanguage().equals("en");
                                // && value.getText().toLowerCase().contains("greta");
                    }
                })
                .print();

        env.execute();
    }
}
