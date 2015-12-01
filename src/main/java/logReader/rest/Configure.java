package logReader.rest;

import logReader.common.LogEvent;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by pkazakov on 11/30/15.
 */
public class Configure extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setContentType("text/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>This will configure cached content based on passed filter..<h1>");
        response.getWriter().println("session=" + request.getSession(true).getId());
    }

}
