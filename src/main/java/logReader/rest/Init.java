package logReader.rest;


import logReader.reader.LogWatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Created by pkazakov on 11/30/15.
 * This servlet will be loaded first.
 */
public class Init extends HttpServlet {
    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        //super(servletConfig);
        //this.s = servletConfig.getInitParameter("myParam");
        try{
            LogWatcher.main(null);
        } catch(Exception e){
            throw new ServletException(e);
        }
    }

}
