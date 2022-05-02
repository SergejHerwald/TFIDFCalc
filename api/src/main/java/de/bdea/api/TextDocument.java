package de.bdea.api;

import java.util.HashMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TextDocument {

    @Id
    private String id;

    private String name;

    private HashMap<String, Integer> wordCounter;

    public TextDocument(String name, HashMap<String,Integer> wordCounter) {
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
