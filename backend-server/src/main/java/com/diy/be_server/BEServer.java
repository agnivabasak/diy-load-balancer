package com.diy.be_server;
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

            Server server = new Server(port);
            ServletContextHandler contextHandler = new ServletContextHandler();
            contextHandler.addServlet(PingHandler.class, "/");

            server.setHandler(contextHandler);
            server.start();
            server.join();
        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
}
