package logReader.reader;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;

import com.google.common.cache.*;
import logReader.common.LogEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
/**
 * Created by noone_000 on 10/17/2015.
 */

// TODO: change from Guava Cache to separate H2 database instance so can do analytics and data cleansing better

public class LogWatcher extends TimerTask implements WatchService {
    static String lastFileLoaded = "";
    static String lastLoadedContent = "";

    // SimpleTimeFormat to
    private static final Logger log = LogManager.getLogger("logWatcher");
    public static Map<String,Exception> unParsable = new LinkedHashMap<String,Exception>();
    static String filer;
    static long from;
    static long to;
    Path pathDir = null;
    //WatchKey key = null;
    static WatchService fileWatch = null;
    boolean refreshFlag = true;

    // default value, overwritten in LogWatcher()
    static String path = "C:/Users/noone_000/Documents/Star Wars - The Old Republic/CombatLogs";
    public static LoadingCache<Long, LogEvent> events = CacheBuilder.newBuilder()
            .maximumSize(1000000)
   //         .expireAfterWrite(10, TimeUnit.MINUTES)
            .build(
                    new CacheLoader<Long, LogEvent>() {
                        public LogEvent load(Long key) throws Exception {
                            return loadBasedOnTime(key);
                        }
                    });

    public LogWatcher() throws IOException {
        fileWatch =   FileSystems.getDefault().newWatchService();
        String content = readFile("./conf/conf.properties");
        String lines[] = content.split("\n");
        for (String line : lines){
            if(line.startsWith("src_log_location")) {
                /*
                int startPos = line.indexOf("=");
                int lastPos = line.length() - 1;
                if (line.charAt(lastPos) == '\r') {
                    path = line.substring(startPos,lastPos);
                } else {
                    path = line.substring(startPos + 1);
                }
                */
                pathDir = Paths.get(path);
                // we only care about files created and changed
                pathDir.register(fileWatch, ENTRY_CREATE, ENTRY_MODIFY);
                log.info(path);
            }
        }
    }

    @Override
    public void run() {
        // see what has changed since from to now
        // filter only things needed
        // return results ()
        log.info("Run has been completed");
        try {
            cacheLoader(refreshFlag);
        }catch(InterruptedException e){
            log.error("Interrupted");
        }
        refreshFlag = false;
    }
    // guava sorted hash map here

    static SortedSet<String> getDirList(){
        String[] listOfFiles = new File(path).list();
        if (listOfFiles == null) {
            listOfFiles = new String[0];
        }
        return new TreeSet<String>(Arrays.asList(listOfFiles));
    }

    public static void main(String...args) throws IOException {
        LogWatcher logWatcher = new LogWatcher();
        //cacheLoader();
        Timer timer =new Timer();
        timer.schedule(logWatcher, 0, 30000);
    }

    public static void cacheLoader(boolean refresh) throws InterruptedException {
        SortedSet<String> allFiles = getDirList();
        SortedSet<String> fileList = new TreeSet<>();
        if(!lastFileLoaded.equals("")) {
            fileList = allFiles.tailSet(lastFileLoaded);
        } else {
            fileList = allFiles;
        }
        Set<String> regex = new LinkedHashSet<String>();
        //regex.add(".*");
        //regex.add(".*<[0-9]{4,}>.*");
        regex.add(".*@Headcracker.*");
        //regex.add(".*Sonic.*");
        // regex.add("\\Q[(.*)?\\E]");
        WatchKey change = fileWatch.poll(5,TimeUnit.SECONDS);
        if(change != null || refresh ) {
            for (String curLine : getLogEntries(null, null, regex, fileList)) {
                try {
                    Long time = parseDateTime(curLine);
                    LogEvent<String> event = EventParser.parseEvent(time, curLine);
                    events.put(time, event);
                } catch (Exception e) {
                    log.error("IO problem:{}", e);
                }
            }
        }

    }

    private static Long parseDateTime(String curLine) {
        int year = 0 ,month = 0 ,day = 0,hour =0 ,min = 0 ,sec = 0 ,ms = 0;
        try {
            log.info("{}", curLine);
            year = Integer.parseInt(curLine.substring(7, 11));
            month = Integer.parseInt(curLine.substring(12, 14));
            day = Integer.parseInt(curLine.substring(15, 17));
            int startIdx = curLine.indexOf("|[") + 2;
            int endIdx = curLine.indexOf("]", startIdx);
            String time = curLine.substring(startIdx, endIdx);
            //09:15:57.024
            //log.info("{}",time);
            hour = Integer.parseInt(time.substring(0, 2));
            min = Integer.parseInt(time.substring(3, 5));
            sec = Integer.parseInt(time.substring(6, 8));
            ms = Integer.parseInt(time.substring(9));
        }catch(Exception e){
            log.info("Line:{} exception{}",curLine,e);
            unParsable.put(curLine,e);

        }
        //log.info("{}{}{} {} {} {} {}",year,month,day,hour,min,sec,ms);
        //log.info("year:{} month:{} day:{} hour:{} min:{} {} {}",year,month,day,hour,min,sec,ms);
        DateTime dateTime = new DateTime(year,month,day,hour,min,sec,ms);
        return dateTime.getMillis();
    }

    static Set<String> getLogEntries(String from,String to,Set<String> patterns, Set<String> fileList) {
        Set<String> result = new LinkedHashSet<String>();
        Set<Pattern> filters = new LinkedHashSet<Pattern>();
        for(String str: patterns){
            filters.add(Pattern.compile(str));
        }
        long entriesFound = 0;
        try{
            for(String file: fileList) {
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
                        lastLoadedContent = file + "|" +str;
                        result.add(lastLoadedContent);
                        entriesFound++;
                    }
                }
                lastFileLoaded = file;
            }
        } catch (IOException e){
            log.info("Error reading file: {}",e);
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

    // WatchService related
    @Override
    public void close() throws IOException {

    }

    @Override
    public WatchKey poll() {
        return null;
    }

    @Override
    public WatchKey poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public WatchKey take() throws InterruptedException {
        return null;
    }
}
