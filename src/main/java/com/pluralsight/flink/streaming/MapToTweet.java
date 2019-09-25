package com.pluralsight.flink.streaming;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.functions.MapFunction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MapToTweet implements MapFunction<String, Tweet> {

    static private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Tweet map(String tweetJsonString) throws Exception {

        JsonNode tweetJson = mapper.readTree(tweetJsonString);
        JsonNode textNode = tweetJson.get("text");
        JsonNode langNode = tweetJson.get("lang");
        List<String> tags = new ArrayList<>();

        JsonNode entities = tweetJson.get("entities");
        if(entities != null){
            JsonNode hashtags = entities.get("hashtags");
            for(Iterator<JsonNode> iter = hashtags.elements(); iter.hasNext();){
                JsonNode node = iter.next();
                String hashtag = node.get("text").asText();
                tags.add(hashtag);
            }
        }

        return new Tweet(langNode == null ? "" : langNode.asText(),
                textNode == null ? "" : textNode.asText(), tags);
    }
}
