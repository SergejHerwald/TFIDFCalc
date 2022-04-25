package de.bdea.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.io.BufferedReader;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TextDocumentController {
	
//	@Autowired
	private TextDocumentRepository repository;

	@PostMapping("/uploadFile")
	public boolean addTextDocument(
			@RequestParam("file") MultipartFile file){
		
		//return value has to be changed HashMap<String, Integer>
		
		try {
			
			InputStream stream = file.getInputStream();
			InputStreamReader isReader = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(isReader);
			StringBuffer sb = new StringBuffer();
			String str;
		    while((str = reader.readLine())!= null){
		    	sb.append(str);
		    }
		    System.out.println(sb.toString());
		    

//			SparkConf conf = new SparkConf().setAppName("word counter").setMaster("local[*]");
//			JavaSparkContext sc = new JavaSparkContext(conf);
		    
//			String [] strs = sb.toString().split(" ");
//			for(String string : strs) {
//				System.out.println(string);
//			}
//			JavaRDD<String> rdd = sc.parallelize(Arrays.asList(strs));
		    
		    
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
		
		return true;	
	}
	
}
