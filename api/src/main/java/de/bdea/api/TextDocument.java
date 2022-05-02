package de.bdea.api;

import java.util.HashMap;
import java.util.List;

import com.kennycason.kumo.WordFrequency;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TextDocument {

    @Id
    private String id;

    private String name;

    private List<WordFrequency> wordCounter;

    public TextDocument(String name, List<WordFrequency> wordCounter) {
        this.name = name;
        this.wordCounter = wordCounter;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
