package logReader.reader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by noone_000 on 10/17/2015.
 */
public class LogWatcher extends TimerTask {
    private static final Logger log = LogManager.getLogger("logWatcher");
    static String filer;
    static long startTime;
    static long endTime;
    static String path="C:/Users/noone_000/Documents/Star Wars - The Old Republic/CombatLogs";

    LoadingCache<String, String> graphs = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<String, String>() {
                        public String load(String key) throws Exception {
                            return loadBasedOnTime(key);
                        }
                    });

    public LogWatcher(){

    }

    @Override
    public void run() {
        // see what has changed since startTime to now
        // filter only things needed
        // return results
    }
    // guava sorted hash map here

    static Set<String> getDirList(){
        String[] listOfFiles= new File(path).list();
        if (listOfFiles == null) {
            listOfFiles = new String[0];
        }
        return new TreeSet<String>(Arrays.asList(listOfFiles));
    }

    public static void main(String...args) throws IOException {
        String mostRecentFile = new String();
        Set<String> fileList = getDirList();
        Set<String> regex = new LinkedHashSet<String>();
        regex.add(".*<[0-9]{5,}>.*");
        regex.add(".*@Republican.*");
        //regex.add(".*Sonic.*");
       // regex.add("\\Q[(.*)?\\E]");
        getLogEntries(null,null,regex,fileList);
    }
    static Set<String> getLogEntries(String from,String to,Set<String> patterns, Set<String> fileList) throws IOException {
        Set<String> result = new LinkedHashSet<String>();
        Set<Pattern> filters = new LinkedHashSet<Pattern>();
        for(String str: patterns){
            filters.add(Pattern.compile(str));
        }
        //Pattern p = Pattern.compile(".*<[0-9]{5,}>.*");
        String mostRecentFile = new String();
        // Set<String> fileList = getDirList();
        for(String file: fileList){
            log.info("File is {}",file);
            mostRecentFile =  file;
            String fileContent = readFile(path+"/"+mostRecentFile);
            // System.out.println(fileContent);

            for(String str : fileContent.split("\r\n")){
                boolean match = true;
                for(Pattern p: filters){
                    Matcher m = p.matcher(str);
                    if(!m.matches()){
                        match = false;
                    }
                }
                if(match){
                    result.add(str);
                    log.info("str:{}",str);
                }
            }
        }
        return result;
    }
    static String readFile(String pathToFile) throws IOException {
        return new String(readAllBytes(get(pathToFile)),"UTF-8");
    }
    static String loadBasedOnTime(String from){
        return "";
    }

}
