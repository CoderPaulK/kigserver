package logReader.rest;

import com.google.common.cache.LoadingCache;
import logReader.common.LogEvent;
import logReader.reader.LogWatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Select extends HttpServlet
{   //  time
    //  dest
    //  source;
    //  ability;
    //  effect;
    //  message;
    static private Set<String> validHeaders=new HashSet<String>();
    public Select(){
        validHeaders.add("time");
        validHeaders.add("dest");
        validHeaders.add("source");
        validHeaders.add("ability");
        validHeaders.add("effect");
        validHeaders.add("message");

    }
    LoadingCache<Long, LogEvent> events = LogWatcher.events;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/json");
        response.setStatus(HttpServletResponse.SC_OK);
        for(Map.Entry<Long,LogEvent> event : events.asMap().entrySet()){
            // convert event into Gson
            // filter events here
            response.getWriter().println();
        }
        response.getWriter().println("<h1>This will return selected data</h1>");
        String query = request.getQueryString();
        response.getWriter().println(processQuery(query));
        response.getWriter().println("session=" + request.getSession(true).getId());
    }
    static String processQuery(String query){
        String result ="";
        String[] headers = query.split("&");
        Map<String,String> validatedHeaders = new HashMap<String,String>();
        for(String curHeader : headers){
            int endHeaderPos = curHeader.indexOf("=");
            String header = curHeader.substring(0, endHeaderPos);
            String value = curHeader.substring(endHeaderPos+1);
            if(!validHeaders.contains(header)){
                return message("<font colorred>Wrong header</red>");
            } else {
                // apply filter..
            }
        }
        //validate query string
        return result;
    }

    static String message(String message){
        return "<b><i>"+message+"</b></i>";
    }
}
