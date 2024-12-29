package com.diy.be_server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;

public class PingHandler extends HttpServlet {
    private final Logger log = LogManager.getLogger(PingHandler.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final long startTime = System.currentTimeMillis();
        log.info("------------------------------------------------------------------------------------------------");
        String ipAddress = req.getHeader("X-Forwarded-For");
        if (ipAddress == null) {
            ipAddress = req.getRemoteAddr();
        }
        String proxyIpAddress = ipAddress.contains(",") ? ipAddress.split(", ")[1] : "None";
        ipAddress = ipAddress.contains(",") ? ipAddress.split(",")[0] : ipAddress;
        log.info("BEServer - /");
        log.info("Received request from "+ipAddress);
        log.info("Request passed through from proxy : "+proxyIpAddress);
        log.info(req.getMethod()+" / "+req.getProtocol());
        try{
            log.info("Proxy Protocol: "+ req.getHeaders("X-Forwarded-Proto").nextElement());
        } catch (NoSuchElementException ex){
            log.info("Proxy Protocol: None");
        }
        log.info("Host: "+ req.getHeaders("Host").nextElement());
        try{
            log.info("Proxy Host: "+ req.getHeaders("X-Forwarded-Host").nextElement());
        } catch (NoSuchElementException ex){
            log.info("Proxy Host: None");
        }
        try{
            log.info("Proxy Port: "+ req.getHeaders("X-Forwarded-Port").nextElement());
        } catch (NoSuchElementException ex){
            log.info("Proxy Port: None");
        }
        log.info("User Agent: " + req.getHeaders("User-Agent").nextElement());
        log.info("Accept: " + req.getHeaders("Accept").nextElement());

        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = resp.getWriter();
        out.println("Response from BEServer");
        log.info("Responded with: \"Response from BEServer\"");
        final long endTime = System.currentTimeMillis();
        log.info("Time required to  process the request: "+(endTime-startTime)+" ms");
        log.info("------------------------------------------------------------------------------------------------");
    }

}
