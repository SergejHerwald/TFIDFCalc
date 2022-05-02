package de.bdea.api;

import java.awt.*;
import java.io.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import scala.Tuple2;

@RestController
public class TextDocumentController {

    @Autowired
    private TextDocumentRepository repository;

    @PostMapping("/api/uploadFile")
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
            SparkConf conf = new SparkConf().setAppName("xy").setMaster("local[*]");

            JavaSparkContext sc = new JavaSparkContext(conf);

            JavaRDD<String> tokens = sc.textFile(String.valueOf(textFilepath)).flatMap(
                    s -> Arrays.asList(s.split("\\W+")).iterator());

            JavaPairRDD<String, Integer> counts = tokens.mapToPair(
                    token -> new Tuple2<>(token, 1)).reduceByKey((x, y) -> x + y);

            Function<Tuple2<String, Integer>, Boolean> filterFunction = w -> (w._1.length() > 4);
            JavaPairRDD<String, Integer> rddF = counts.filter(filterFunction);


            List<Tuple2<String, Integer>> results = rddF.collect();
            results.forEach(System.out::println);
            System.out.println("\n");


            List<WordFrequency> wordFrequencies = new ArrayList<WordFrequency>();

            for (Iterator iterator = results.iterator(); iterator.hasNext(); ) {
                Tuple2<String, Integer> tuple2 = (Tuple2<String, Integer>) iterator.next();
                wordFrequencies.add(new WordFrequency(tuple2._1, tuple2._2));
            }

            sc.close();

            drawImage(wordFrequencies, Objects.requireNonNull(file.getOriginalFilename()));
            TextDocument test = new TextDocument(file.getOriginalFilename(), wordFrequencies);
            repository.insert(test);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void drawImage(List<WordFrequency> wordFrequencies, String fileName) throws IOException {
        final Dimension dimension = new Dimension(600, 600);
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wordCloud.setPadding(2);
        wordCloud.setBackground(new CircleBackground(300));
        wordCloud.setColorPalette(new ColorPalette(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE));
        wordCloud.setFontScalar(new SqrtFontScalar(2, 40));
        wordCloud.build(wordFrequencies);
        File image = new File("./src/main/webapp/WEB-INF/images/" + fileName.substring(0, fileName.lastIndexOf('.')) + ".png");
        image.getParentFile().mkdirs();
        image.createNewFile();
        System.out.println(image.getAbsolutePath());
        wordCloud.writeToFile(image.getPath());
        //System.out.print(wordFrequencies);
    }

    @GetMapping("/api/getFiles")
    public String[] getFiles() {
    	//repository.findAll();
        String fileName;
    	List<String> names = new ArrayList<String>();
//    	 List<String> strs = new ArrayList<String>();
//         for (Iterator iterator = tx.iterator(); iterator.hasNext();) {
//        	TextDocument doc = (TextDocument) iterator.next();
// 			strs.add(doc.getName());
// 		 }
//        String [] names = (String[]) strs.toArray();
//        return names;
        File dir = new File("./textfiles"); // current directory
        File[] files = dir.listFiles();

        assert files != null;
        for (File file : files) {
            fileName = file.getName();

            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            names.add(fileName);
        }
        Object[] test = names.toArray();
        System.out.println(test[1]);

		return Arrays.copyOf(test, test.length, String[].class);
    }

    @GetMapping("/api/startBatchWork")
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
