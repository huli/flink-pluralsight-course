package com.pluralsight.flink.streaming;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.util.Collector;

import java.util.Properties;

public class LanguageControlStream {

    public static void main(String [] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);

        Properties props = TwitterConfiguration.getProperties();

        // Define the control stream
        DataStream<LanguageConfig> controlStream = env.socketTextStream("localhost", 9876)
                .flatMap(new FlatMapFunction<String, LanguageConfig>() {
                    @Override
                    public void flatMap(String value, Collector<LanguageConfig> collector) throws Exception {
                        for(String langConfig : value.split(",")){
                            String [] kvPair = langConfig.split("=");
                            collector.collect(new LanguageConfig(kvPair[0], Boolean.parseBoolean(kvPair[1])));
                        }
                    }
                });

        // Define data stream and connect with control stream
        env.addSource(new TwitterSource(props))
                .map(new MapToTweet())
                .keyBy(new KeySelector<Tweet, String>() {
                    @Override
                    public String getKey(Tweet value) throws Exception {
                        return value.getLanguage();
                    }
                })
                .connect(controlStream.keyBy(new KeySelector<LanguageConfig, String>() {
                    @Override
                    public String getKey(LanguageConfig value) throws Exception {
                        return value.getLanguage();
                    }
                }))
                .flatMap(new RichCoFlatMapFunction<Tweet, LanguageConfig, Tuple2<String, String>>() {

                    ValueStateDescriptor<Boolean> shouldProcess = new ValueStateDescriptor<Boolean>("languageConfig", Boolean.class);

                    @Override
                    public void flatMap1(Tweet value, Collector<Tuple2<String, String>> collector) throws Exception {

                        Boolean processLanguage = getRuntimeContext().getState(shouldProcess).value();
                        if(processLanguage != null && processLanguage){
                            for(String tag : value.getTags()){
                                collector.collect(new Tuple2<>(value.getLanguage(), tag));
                            }
                        }
                    }

                    @Override
                    public void flatMap2(LanguageConfig value, Collector<Tuple2<String, String>> out) throws Exception {
                        getRuntimeContext().getState(shouldProcess).update(value.isShouldProcess());
                    }
                })
                .print();

        env.execute();
    }

    static class LanguageConfig {

        private String language;
        private Boolean shouldProcess;

        public LanguageConfig(String language, boolean shouldProcess){
            this.language = language;
            this.shouldProcess = shouldProcess;
        }

        public String getLanguage(){
            return language;
        }

        public Boolean isShouldProcess(){
            return shouldProcess;
        }

    }
}

