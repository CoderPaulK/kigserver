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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

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
        Map<Long,LogEvent> resEvents = new HashMap<Long,LogEvent>();
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
                    validatedHeaders.put(header, value);
                }
            }
            if (!validHeaders.contains(header)) {
                return message("<font color=red>Wrong header:" + header + "</red>");
            }
        }

        for (Map.Entry<Long, LogEvent> event : events.asMap().entrySet()) {
            LogEvent logEvent = event.getValue();
            try {
                boolean selected = true;
                for(Map.Entry<String,String>curHeader :validatedHeaders.entrySet()){
                // ensure header has value
                    Method getMethod= logEvent.getClass().getMethod("get"+capitalize(curHeader.getKey()),null);
                    String fieldValue = (String) getMethod.invoke(logEvent,null);
                    if (! fieldValue.contains(curHeader.getValue())) {
                        selected = false;
                        break;
                       // resEvents.put(event.getKey(), event.getValue());
                    }
                }
                if(selected){
                    resEvents.put(event.getKey(), event.getValue());
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

        }
        //validate query string
        //return jsonObject.toString();
        return output;
    }
    private static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
    static String message(String message){
        return "<b><i>"+message+"</b></i>";
    }
    static Long convertTime(String line){
        String year = line.substring(0,3);
        String month = line.substring(4,5);
        String day = line.substring(6,7);
        String hour = line.substring(8,9);
        String min = line.substring(10);
        // compose date and convert it into milliseconds.
        return 0l;
    }
}
