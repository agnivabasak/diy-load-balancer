package com.diy.load_balancer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class BEServer
{
    private static final Logger log = LogManager.getLogger(BEServer.class);

    public static void main( String[] args )
    {
        try{
            int port = args.length>0?Integer.parseInt(args[0]):8081;
            log.info("STARTING Backend Server (Jetty Server) at port "+port);

            //Servlets and Jetty Servers are multithreaded by default, so there is no need to implement multithreading on my own
            Server server = new Server(port);
            ServletContextHandler contextHandler = new ServletContextHandler();
            contextHandler.setAttribute("be.port",port);
            contextHandler.addServlet(PingHandler.class, "/ping");

            server.setHandler(contextHandler);
            server.start();
            server.join();
        } catch(Exception e){
            log.error(e.getMessage());
        }
    }
}
