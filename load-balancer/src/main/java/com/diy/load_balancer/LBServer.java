package com.diy.load_balancer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.io.File;
import java.util.Scanner;

public class LBServer
{
    private static final Logger log = LogManager.getLogger(LBServer.class);

    public static void main( String[] args )
    {
        try{
            //Here if any configuration is missing, it is assumed that the defaults are
            //LBServer = http://localhost:8080
            //Default LBAlgo = Round Robin
            //BEServerList = ["http://localhost:8081"]
            //health check interval = 10 seconds
            int port = args.length>0?Integer.parseInt(args[0]):8080;
            log.info("STARTING Load Balancer Frontend (Jetty Server) at port "+port);
            //Servlets and Jetty Servers are multithreaded by default, so there is no need to implement multithreading on my own
            Server server = new Server(port);
            ServletContextHandler contextHandler = new ServletContextHandler();

            LBAlgoEnum selectedAlgo = args.length>1?LBAlgoEnum.valueOf(args[1]):LBAlgoEnum.ROUND_ROBIN;
            String backendServers = args.length>2?getCommaSeparatedBackendUrls(args[2]):"http://localhost:8081";
            Integer healthCheckInterval = args.length>3?Integer.parseInt(args[3]):10;

            LBAlgo lbAlgo = new LBAlgo(selectedAlgo, backendServers, healthCheckInterval);

            contextHandler.setAttribute("lb.lbAlgo", lbAlgo);
            contextHandler.setAttribute("lb.port",port);
            contextHandler.addServlet(PingHandler.class, "/ping");

            server.setHandler(contextHandler);
            server.start();
            server.join();
        } catch(Exception e){
            log.error(e.getMessage());
        }
    }

    private static String getCommaSeparatedBackendUrls(String urlsPath){
        StringBuilder urls = new StringBuilder();
        try{
            File urlsFile = new File(urlsPath);
            Scanner sc = new Scanner(urlsFile);
            while(sc.hasNextLine()){
                urls.append(sc.nextLine()).append(",");
            }
            if(urls.isEmpty()){
                throw new Exception("The file provided isn't formatted properly");
            } else{
                urls.deleteCharAt(urls.length()-1);
            }
        } catch (Exception e){
            log.error("Exception while parsing the backend urls file: "+e.getMessage());
            urls = new StringBuilder("http://localhost:8081");
        }
        return urls.toString();
    }
}
