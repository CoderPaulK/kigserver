package logReader.rest;

import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import logReader.common.LogEvent;
import logReader.reader.LogWatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;
import org.joda.time.DateTime;


public class Select extends HttpServlet
{   //  time
    //  dest
    //  source;
    //  ability;
    //  effect;
    //  message;
    private static SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Logger log = LogManager.getLogger("Select");
    static private Set<String> validHeaders=new HashSet<String>();
    public Select(){
        validHeaders.add("from");
        validHeaders.add("to");
        validHeaders.add("time");
        validHeaders.add("dest");
        validHeaders.add("source");
        validHeaders.add("ability");
        validHeaders.add("effect");
        validHeaders.add("message");
        validHeaders.add("output");
        validHeaders.add("res_size");
    }
    static LoadingCache<Long, LogEvent> events = LogWatcher.events;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/json");
        response.setStatus(HttpServletResponse.SC_OK);
        String query = request.getQueryString();
        response.getWriter().println(processQuery(query));
        //response.getWriter().println("session=" + request.getSession(true).getId());
    }
    static String processQuery(String query){

        String result ="";
        if (query == null || query.equals("")){
            return "";
        }
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        Map<Long,LogEvent> resEvents = new TreeMap<Long,LogEvent>();
        String[] headers = query.split("&");
        Map<String,String> validatedHeaders = new HashMap<String,String>();
        Map<String,Long> timeFrame =  new HashMap();

        for(String curHeader : headers) {
            int endHeaderPos = curHeader.indexOf("=");
            String header = curHeader.substring(0, endHeaderPos);
            String value = curHeader.substring(endHeaderPos + 1);
            if(value.length()>0) {
                if(header.equals("to") || header.equals("from")) {
                    timeFrame.put(header, convertTime(value));
                } else {
                    value = URLDecoder.decode(value);
                    validatedHeaders.put(header, value);
                }
            }
            if (!validHeaders.contains(header)) {
                return message("<font color=red>Wrong header:" + header + "</red>");
            }
        }
        boolean jsonOutput = false;
        if(validatedHeaders.get("output")!=null){
            validatedHeaders.remove("output");
            jsonOutput = true;
        }

        int maxSize = 5000;
        if(validatedHeaders.get("res_size")!=null){
            try{
                maxSize=Integer.parseInt(validatedHeaders.get("res_size"));
                validatedHeaders.remove("res_size");
            } catch (NumberFormatException e) {
                log.error("Can't convert value {} to int",validatedHeaders.get("res_size"));
            }
        }

        for (Map.Entry<Long, LogEvent> event : events.asMap().entrySet()) {
            LogEvent logEvent = event.getValue();
            try {
                if(((timeFrame.get("from")!= null && timeFrame.get("from") < event.getKey()) || timeFrame.get("from") == null) &&
                        ((timeFrame.get("to")!= null && timeFrame.get("to") > event.getKey())||timeFrame.get("to") == null)) {
                    boolean selected = true;
                    for (Map.Entry<String, String> curHeader : validatedHeaders.entrySet()) {
                        // ensure header has value
                        Method getMethod = logEvent.getClass().getMethod("get" + capitalize(curHeader.getKey()), null);
                        String fieldValue = (String) getMethod.invoke(logEvent, null);
                        if (!fieldValue.contains(curHeader.getValue())) {
                            selected = false;
                            break;
                        }
                    }
                    if (selected) {
                        resEvents.put(event.getKey(), event.getValue());
                    }
                }
            }catch(Exception e){
              return "<b><i>Exception occured:"+e+"</b></i>";
            }
            // convert event into Gson
            //jsonObject.add(event.getKey().toString(),gson.toJsonTree(event.getValue()));
        }

        int counter = 0;
        String output ="Date,Volume\n";
        for (Map.Entry<Long, LogEvent> event : resEvents.entrySet()){
            String dateStr = formater.format(new Date(event.getKey()));
            output += dateStr + ","+event.getValue().getMessage()+"\n";
            jsonObject.add(event.getKey().toString(),gson.toJsonTree(event.getValue()));
            if(counter > maxSize ) {
                break;
            }
            counter++;
        }
        log.info("Done processing size:{}", resEvents.size());
        //validate query string
        if(jsonOutput) {
            return jsonObject.toString();
        }
        return output;
    }
    private static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
    static String message(String message){
        return "<b><i>"+message+"</b></i>";
    }
    static Long convertTime(String line){
        int year = Integer.parseInt(line.substring(0,4));
        int month = Integer.parseInt(line.substring(4,6));
        int day = Integer.parseInt(line.substring(6,8));
        int hour = Integer.parseInt(line.substring(8,10));
        int min = Integer.parseInt(line.substring(10));
        // compose date and convert it into milliseconds.
        log.info("year:{} month:{} day:{} hour:{} min:{}",year,month,day,hour,min);
        DateTime dateTime = new DateTime(year,month,day,hour,min);
        return dateTime.getMillis();
    }
}
