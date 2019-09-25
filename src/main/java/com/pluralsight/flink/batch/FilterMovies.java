package com.pluralsight.flink.batch;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.java.utils.ParameterTool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FilterMovies {

    public static void main(String[] args) throws Exception{

        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        ParameterTool parameters = ParameterTool.fromArgs(args);
        String input = parameters.getRequired("input");
        String output= parameters.getRequired("output");

        env.readCsvFile(input)
                    .ignoreFirstLine()
                    .parseQuotedStrings('"')
                    .ignoreInvalidLines()
                    .types(Long.class, String.class, String.class)
                .map(new MapFunction<Tuple3<Long, String, String>, Movie>() {
                    @Override
                    public Movie map(Tuple3<Long, String, String> line) throws Exception {

                        String movieName = line.f1;
                        String[] genres = line.f2.split("\\|");

                        return new Movie(movieName, new HashSet<>(Arrays.asList(genres)));
                    }})
                .filter(new FilterFunction<Movie>() {
                    @Override
                    public boolean filter(Movie movie) throws Exception {
                        return movie.getGenres().contains("Drama");
                    }
                })
                .writeAsText(output);

        // Execute the processing plan
        env.execute();
    }
}

class Movie
{
    private String name;
    private Set<String> genres;

    public Movie(String name, Set<String> genres){
        this.name = name;
        this.genres = genres;
    }

    public String getName() { return name; }

    public Set<String> getGenres() { return genres;}

    @Override
    public String toString(){
        return "Movie{" +
                "name='" + name + '\'' +
                ", genres=" + genres +
                '}';
    }
}
