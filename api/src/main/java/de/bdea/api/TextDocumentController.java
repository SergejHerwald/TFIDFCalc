package de.bdea.api;

import java.awt.*;
import java.io.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
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
    // variables used for Tag-Clouds
    private static final Dimension dimension = new Dimension(600, 600);
    private static final int padding = 4;
    private static final ColorPalette colors = new ColorPalette(Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE);
    private static final CircleBackground backGround = new CircleBackground(300);
    private static final SqrtFontScalar fontScalar = new SqrtFontScalar(2, 40);

    @Autowired
    private TextDocumentRepository repository;

    @PostMapping("/api/uploadFile")
    public boolean addTextDocument(
            @RequestParam("file") MultipartFile file) throws IOException {


        try {
            // store textfiles in file system
            Path dir_textfiles = Path.of("./textfiles/");
            InputStream stream = file.getInputStream();
            String fileName = Objects.requireNonNull(file.getOriginalFilename());
            Path textFilepath = Paths.get(dir_textfiles.toString(), fileName);
            File textFile = new File(String.valueOf(textFilepath));
            System.out.println(textFile.getPath());
            textFile.getParentFile().mkdirs();
            textFile.createNewFile();
            System.out.println(textFile.getAbsolutePath());
            file.transferTo(textFilepath);

            // TF:
            // going through text Data, turning every word to lower case to reduce word doubles
            // counting and filtering all words with more than 4 letters
            SparkConf conf = new SparkConf().setAppName("xy").setMaster("local[*]");
            JavaSparkContext sc = new JavaSparkContext(conf);
            JavaRDD<String> tokens = sc.textFile(String.valueOf(textFilepath)).flatMap(
                    s -> Arrays.asList(s.toLowerCase().split("[^\\S\\r\\n]")).iterator());
            JavaPairRDD<String, Integer> counts = tokens.mapToPair(
                    token -> new Tuple2<>(token, 1)).reduceByKey(Integer::sum);

            Function<Tuple2<String, Integer>, Boolean> filterFunction = w -> (w._1.length() > 4);
            JavaPairRDD<String, Integer> rddF = counts.filter(filterFunction);
            List<Tuple2<String, Integer>> results = rddF.collect();
            results.forEach(System.out::println);
            System.out.println("\n");
            sc.close();

            // creating Tag-Cloud
            List<WordFrequency> wordFrequencies = new ArrayList<>();
            List<WordFrequency> tf = new ArrayList<>();
            for (Tuple2<String, Integer> tuple2 : results) {
                tf.add(new WordFrequency(tuple2._1, tuple2._2));
                // TODO: tuple2._2 / log(DF); if DF = 0, tuple2._2 / 1
                // Need DF!!!!
                int t2 = tuple2._2;
                wordFrequencies.add(new WordFrequency(tuple2._1, t2));
            }
            drawImage(wordFrequencies, fileName);

            // saves TF in DB for later usage
            TextDocument textDocument = new TextDocument(file.getOriginalFilename(), tf);
            repository.insert(textDocument);

            // TODO: refresh list with Tag Clouds frontend

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    private void drawImage(List<WordFrequency> wordFrequencies, String fileName) throws IOException {
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wordCloud.setPadding(padding);
        wordCloud.setBackground(backGround);
        wordCloud.setColorPalette(colors);
        wordCloud.setFontScalar(fontScalar);
        wordCloud.build(wordFrequencies);
        File image = new File("./src/main/webapp/WEB-INF/images/" + fileName.substring(0, fileName.lastIndexOf('.')) + ".png");
        image.getParentFile().mkdirs();
        image.createNewFile();
        wordCloud.writeToFile(image.getPath());
    }

    @GetMapping("/api/getFiles")
    public String[] getFiles() {
        // TODO: need cleanup
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
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TODO: Implementaion of batch work
        // get DF as ArrayList out of DB (or create if none exists)
        // get all TF out of DB
        // add or increase DF for Words from all TF
        // Update DF in DB
        // Redo all Tag-Clouds with TF (already out of DB) and new DF
        //
        // global Tag-Cloud:
        // all TF in one ArrayList globalTF (if same Word, add frequency together)
        // create global Tag-Cloud with globalTF and new DF
        // store in File image = new File("./src/main/webapp/WEB-INF/images/global Tag Cloud.png");
    }


}
