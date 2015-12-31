package logReader.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import logReader.common.LogEvent;
import logReader.reader.EventParser;
import logReader.reader.LogWatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by pkazakov on 12/1/15.
 */
public class ParseErrors extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/json");
        response.setStatus(HttpServletResponse.SC_OK);
        Gson gson = new Gson();
        JsonObject jsonObject = new JsonObject();
        try {

            for (Map.Entry<String,Exception> event: LogWatcher.unParsable.entrySet()) {
                // convert event into Gson
                jsonObject.add(event.getKey().toString(),gson.toJsonTree(event.getValue()));
            }
        }catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        response.getWriter().append(jsonObject.toString());
    }
}
