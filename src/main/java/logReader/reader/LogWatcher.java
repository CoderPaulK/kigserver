package logReader.reader;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import com.google.common.cache.*;
import com.google.common.collect.ImmutableMap;
import logReader.common.LogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
/**
 * Created by noone_000 on 10/17/2015.
 */
public class LogWatcher extends TimerTask {
    // SimpleTimeFormat to
    private static final Logger log = LogManager.getLogger("logWatcher");
    static String filer;
    static long from;
    static long to;

    // default value, overwritten in LogWatcher()
    static String path = "C:/Users/noone_000/Documents/Star Wars - The Old Republic/CombatLogs";
    public static LoadingCache<Long, LogEvent> events = CacheBuilder.newBuilder()
            .maximumSize(1000000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<Long, LogEvent>() {
                        public LogEvent load(Long key) throws Exception {
                            return loadBasedOnTime(key);
                        }
                    });

    public LogWatcher() throws IOException {
        String content = readFile("./conf/conf.properties");
        String lines[] = content.split("\n");
        for (String line : lines){
            if(line.startsWith("src_log_location")){
                int startPos = line.indexOf("=");
                path =  line.substring(startPos+1);
                log.info(path);
            }
        }
    }

    @Override
    public void run() {
        // see what has changed since from to now
        // filter only things needed
        // return results ()
        cacheLoader();
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
        LogWatcher logWatcher = new LogWatcher();
        Timer timer =new Timer();
        timer.schedule(logWatcher,0,30000);
    }

    public static void cacheLoader(){
        Set<String> fileList = getDirList();
        Set<String> regex = new LinkedHashSet<String>();
        regex.add(".*");
        //regex.add(".*<[0-9]{4,}>.*");
        //regex.add(".*@Republican.*");
        //regex.add(".*Sonic.*");
        // regex.add("\\Q[(.*)?\\E]");
        try {
            for(String curLine: getLogEntries(null,null,regex,fileList)){
                Long time = parseDateTime(curLine);
                LogEvent<String> event = EventParser.parseEvent(time, curLine);
                events.put(time,event);
            }
        } catch (IOException e) {
            log.error("IO problem:{}",e);
        }
    }

    private static Long parseDateTime(String curLine) {
        log.info("{}",curLine);
        int year = Integer.parseInt(curLine.substring(7, 11));
        int month = Integer.parseInt(curLine.substring(12, 14));
        int day = Integer.parseInt(curLine.substring(15, 17));
        int startIdx = curLine.indexOf("|[")+2;
        int endIdx = curLine.indexOf("]", startIdx);
        String time = curLine.substring(startIdx,endIdx);
        //09:15:57.024
        //log.info("{}",time);
        int hour = Integer.parseInt(time.substring(0,2));
        int min = Integer.parseInt(time.substring(3,5));
        int sec = Integer.parseInt(time.substring(6,8));
        int ms = Integer.parseInt(time.substring(9));
        //log.info("{}{}{} {} {} {} {}",year,month,day,hour,min,sec,ms);
        DateTime dateTime = new DateTime(year,month,day,hour,min,sec,ms);
        return dateTime.getMillis();
    }

    static Set<String> getLogEntries(String from,String to,Set<String> patterns, Set<String> fileList) throws IOException {
        Set<String> result = new LinkedHashSet<String>();
        Set<Pattern> filters = new LinkedHashSet<Pattern>();
        for(String str: patterns){
            filters.add(Pattern.compile(str));
        }

        long entriesFound = 0;
        for(String file: fileList){
            log.info("File is {}",file);
            String fileContent = readFile(path+"/"+file);

            for(String str : fileContent.split("\r?\n")){
                boolean match = true;
                for(Pattern p: filters){
                    Matcher m = p.matcher(str);
                    if(!m.matches()){
                        match = false;
                    }
                }
                if(match){
                    result.add(file+"|"+str);
                    entriesFound++;
                }
            }
        }
        log.info("There was {} matching entries found in logs",entriesFound);
        return result;
    }
    static String readFile(String pathToFile) throws IOException {
        return new String(readAllBytes(get(pathToFile)),"UTF-8");
    }
    static LogEvent loadBasedOnTime(Long from){
        return null;
    }

}
