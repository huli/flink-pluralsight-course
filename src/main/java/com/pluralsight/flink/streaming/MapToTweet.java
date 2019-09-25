package com.pluralsight.flink.streaming;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.MapFunction;

public class MapToTweet implements MapFunction<String, Tweet> {

    static private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Tweet map(String tweetJsonString) throws Exception {

        JsonNode tweetJson = mapper.readTree(tweetJsonString);
        JsonNode textNode = tweetJson.get("text");
        JsonNode langNode = tweetJson.get("lang");

        return new Tweet(langNode == null ? "" : langNode.asText(),
                textNode == null ? "" : textNode.asText());
    }
}
