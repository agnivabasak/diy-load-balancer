package com.diy.load_balancer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class LBServer
{
    private static final Logger log = LogManager.getLogger(LBServer.class);

    public static void main( String[] args )
    {
        try{
            int port = args.length>0?Integer.parseInt(args[0]):8080;
            log.info("STARTING Load Balancer Frontend (Jetty Server) at port "+port);
            Server server = new Server(port);
            ServletContextHandler contextHandler = new ServletContextHandler();

            int bePort = 8081; //temporarily assigning it statically
            contextHandler.setAttribute("be.port",bePort);
            contextHandler.setAttribute("lb.port",port);
            contextHandler.addServlet(PingHandler.class, "/");

            server.setHandler(contextHandler);
            server.start();
            server.join();
        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
}
