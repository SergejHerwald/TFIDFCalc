package de.bdea.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kennycason.kumo.WordFrequency;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import scala.Tuple2;

@Document
public class TextDocument {

    @Id
    private String id;

    private String name;

    private Map<String, Integer> wordCounter;

    public TextDocument(String name, Map<String, Integer> wordCounter) {
        this.name = name;
        this.wordCounter = wordCounter;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getWordCounter() {
        return this.wordCounter;
    }

    ;
}
