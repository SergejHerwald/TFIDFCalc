package de.bdea.api;

import com.kennycason.kumo.WordFrequency;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import scala.Tuple2;

import java.util.List;
import java.util.Map;

@Document
public class DocumentFrequency {

    @Id
    private String id;
    private String name;
    private Map<String, Integer> wordCounter;

    public DocumentFrequency(Map<String, Integer> wordCounter, String name) {
        this.wordCounter = wordCounter;
        this.name = name;
    }

    public Map<String, Integer> getWordCounter() {
        return wordCounter;
    }
    public void setWordCounter(Map<String, Integer> wordCounter) {
        this.wordCounter = wordCounter;
    }

    public String getName() {
        return this.name;
    }
}
