package com.pluralsight.flink.streaming;

import org.apache.flink.streaming.connectors.twitter.TwitterSource;

import java.util.Properties;

public class TwitterConfiguration {

    public static Properties getProperties(){
        Properties props = new Properties();
        props.setProperty(TwitterSource.CONSUMER_KEY, "...");
        props.setProperty(TwitterSource.CONSUMER_SECRET, "...");
        props.setProperty(TwitterSource.TOKEN, "...");
        props.setProperty(TwitterSource.TOKEN_SECRET, "...");

        return props;
    }
}
