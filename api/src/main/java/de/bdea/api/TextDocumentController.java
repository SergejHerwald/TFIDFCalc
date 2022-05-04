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
    private TextDocumentRepository tfRepository;
    @Autowired
    private DocumentFrequencyRepository dfRepository;
    private final String dfName = "DF";


    @PostMapping("/api/uploadFile")
    public boolean addTextDocument(
            @RequestParam("file") MultipartFile file) throws IOException {


        try {
            // initial creation of  DF in DB
            if(dfRepository.findByName(dfName) == null){
                Map<String, Integer> m = new HashMap<>();
                dfRepository.insert(new DocumentFrequency(m,dfName));
            }

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
            Map<String, Integer> resultsMap = results.stream().collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));
            // saves TF in DB for later usage
            TextDocument textDocument = new TextDocument(fileName, resultsMap);
            if(tfRepository.findByName(fileName) == null){
                tfRepository.save(textDocument);
            }else{
                tfRepository.delete(tfRepository.findByName(fileName));
                tfRepository.save(textDocument);
            }

            // creating Tag-Cloud
            //System.out.println(mapToWF(resultsMap));
            drawImage(mapToWF(resultsMap), fileName);


            List<TextDocument> t = tfRepository.findAll();
            for (TextDocument textD : t) {
                System.out.println(textD.getName());
            }

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
        List<TextDocument> textDocuments = tfRepository.findAll();
        List<String> names = new ArrayList<String>();
        for (TextDocument t : textDocuments) {
            String name = t.getName();
            name = name.substring(0, name.lastIndexOf('.'));
            names.add(name);
        }
        return names.toArray(String[]::new);
    }

    @GetMapping("/api/startBatchWork")
    public void startBatchWork() throws IOException {
        System.out.println("Batchwork started!!");


    }
    private List<WordFrequency> mapToWF(Map<String, Integer> resultsMap){
        List<WordFrequency> wordFrequencies = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : resultsMap.entrySet()) {
            int df = getDF(entry.getKey());
            wordFrequencies.add(new WordFrequency(entry.getKey(),entry.getValue()/getDF(entry.getKey())));
        }
        return wordFrequencies;
    }
    private int getDF(String word){
        DocumentFrequency df = dfRepository.findByName(dfName);
        Map<String, Integer> dfMap = df.getWordCounter();
        if(dfMap.containsKey(word)){
            return (int)Math.log(dfMap.get(word));
        }
        return 1;
    }

}
