package de.bdea.api;

import com.kennycason.kumo.WordFrequency;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class DocumentFrequency {

    @Id
    private String id;
    private List<WordFrequency> wordCounter;

    public DocumentFrequency(List<WordFrequency> wordCounter){
        this.wordCounter = wordCounter;
    }

    public List<WordFrequency> getWordCounter(){
        return wordCounter;
    }
}
