package logReader.rest;

import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import logReader.common.LogEvent;
import logReader.reader.LogWatcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class All extends HttpServlet
{
    LoadingCache<Long, LogEvent> events = LogWatcher.events;
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/json");
        response.setStatus(HttpServletResponse.SC_OK);
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        try {

            for (Map.Entry<Long, LogEvent> event : events.asMap().entrySet()) {
                // convert event into Gson
                jsonObject.add(event.getKey().toString(),gson.toJsonTree(event.getValue()));
            }
        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        response.getWriter().append(jsonObject.toString());
        //response.getWriter().println("<h1>This will return all the data</h1>");
        //response.getWriter().println("session=" + request.getSession(true).getId());
    }
}
