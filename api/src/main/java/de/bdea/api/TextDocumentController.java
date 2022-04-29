package de.bdea.api;

import java.awt.*;
import java.io.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyFileLoader;
import com.kennycason.kumo.palette.ColorPalette;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import scala.Tuple2;

@RestController
public class TextDocumentController {

    //	@Autowired
    private TextDocumentRepository repository;

    @PostMapping("/uploadFile")
    public boolean addTextDocument(
            @RequestParam("file") MultipartFile file) throws IOException {

        //return value has to be changed to HashMap<String, Integer>

        try {
            Path dir_textfiles = Path.of("./textfiles/");
            InputStream stream = file.getInputStream();
             /*
            TODO
            file.getOriginalFilename() should be Entered name (TextDocument.name)
             */
            Path textFilepath = Paths.get(dir_textfiles.toString(), file.getOriginalFilename());
            File textFile = new File(String.valueOf(textFilepath));
            System.out.println(textFile.getPath());
            textFile.getParentFile().mkdirs();
            textFile.createNewFile();
            System.out.println(textFile.getAbsolutePath());
            file.transferTo(textFilepath);


            //
            //Going trough a text Data and filtering all words with less then 4 letters
            //
            SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("xy");
            //SparkConf conf = SparkSession.builder().appName("ANewUnitTest").master("local").getOrCreate();
            
            JavaSparkContext sc = new JavaSparkContext(conf);


            //saving all words in a List of Array
            //problem is that \W+ is not working with special characters
            //therefore they are split into two words and the special characters are ignored
            JavaRDD<String> tokens = sc.textFile(String.valueOf(textFilepath)).flatMap(
                    s -> Arrays.asList(s.split("\\W+")).iterator());

            //filtering all words with less then 4 letters
            JavaRDD<String> filterer = tokens.filter(new Function<String, Boolean>() {
                @Override
                public Boolean call(String s) throws Exception {
                    return s.length() < 4;
                }
            });

            //counting similar words
            JavaPairRDD<String, Integer> counts = filterer.mapToPair(
                    token -> new Tuple2<>(token, 1)).reduceByKey((x, y) -> x+y);

            List<Tuple2<String, Integer>> results = counts.collect();
            results.forEach(System.out::println);

            sc.close();

            /*
            Word Count File needs to be in this format to work for Word Cloud
                    100: frog
                    94: dog
                    43: cog
                    20: bog
                    3: fog
                    1: log
                    1: pog
            TODO
            file.getOriginalFilename() should be Entered name (TextDocument.name)
             */
            Path dir_count = Path.of("./count/");
            Path countFilepath = Paths.get(dir_count.toString(), file.getOriginalFilename());
            File countFile = new File(String.valueOf(countFilepath));
            countFile.getParentFile().mkdirs();
            countFile.createNewFile();
            System.out.println(countFile.getAbsolutePath());
            drawImage(countFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void drawImage(File wordFrequency) throws IOException {
        final FrequencyFileLoader frequencyFileLoader = new FrequencyFileLoader();
        final List<WordFrequency> wordFrequencies = frequencyFileLoader.load(wordFrequency);
        final Dimension dimension = new Dimension(600, 600);
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wordCloud.setPadding(2);
        wordCloud.setBackground(new CircleBackground(300));
        wordCloud.setColorPalette(new ColorPalette(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE));
        wordCloud.setFontScalar(new SqrtFontScalar(2, 40));
        wordCloud.build(wordFrequencies);
        /*
        TODO
        name should be Entered name (TextDocument.name)
         */
        String name = wordFrequency.getName();
        File image = new File("./images/"+name.substring(0, name.lastIndexOf('.')) + ".png");
        image.getParentFile().mkdirs();
        image.createNewFile();
        System.out.println(image.getAbsolutePath());
        wordCloud.writeToFile(image.getPath());
        //System.out.print(wordFrequencies);
    }
    
    @GetMapping("/getFiles")
    public String[] getFiles() {
    	String[] strs = {"1","2","3","4"}; // has to be replaced with real names of files
		return strs; 	
    }
    
    @GetMapping("/startBatchWork")
    public void startBatchWork() {
    	System.out.println("Batchwork started!!");
    	try {
			Thread.sleep(4000);  // only for test
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	// TODO: Implementaion of batch work
    }


}
