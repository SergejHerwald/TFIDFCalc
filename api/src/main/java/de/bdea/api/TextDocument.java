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
	
}
