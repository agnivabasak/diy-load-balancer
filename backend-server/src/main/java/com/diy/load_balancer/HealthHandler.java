package com.diy.load_balancer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.util.NoSuchElementException;

public class HealthHandler extends HttpServlet {

    private int bePort;
    private final Logger log = LogManager.getLogger(HealthHandler.class);

    @Override
    public void init() throws ServletException
    {
        this.bePort = (int) getServletContext().getAttribute("be.port");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try {
            final long startTime = System.currentTimeMillis();
            log.info("------------------------------------------------------------------------------------------------");
            String ipAddress = req.getHeader("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = req.getRemoteAddr();
            }
            String proxyIpAddress = ipAddress.contains(",") ? ipAddress.split(", ")[1] : "None";
            ipAddress = ipAddress.contains(",") ? ipAddress.split(",")[0] : ipAddress;
            log.info("BEServer (" + bePort + ") - /health");
            log.info("Received request from " + ipAddress);
            log.info("Request passed through from proxy : " + proxyIpAddress);
            log.info(req.getMethod() + " / " + req.getProtocol());
            try {
                log.info("Proxy Protocol: " + req.getHeaders("X-Forwarded-Proto").nextElement());
            } catch (NoSuchElementException ex) {
                log.info("Proxy Protocol: None");
            }
            log.info("Host: " + req.getHeaders("Host").nextElement());
            try {
                log.info("Proxy Host: " + req.getHeaders("X-Forwarded-Host").nextElement());
            } catch (NoSuchElementException ex) {
                log.info("Proxy Host: None");
            }
            try {
                log.info("Proxy Port: " + req.getHeaders("X-Forwarded-Port").nextElement());
            } catch (NoSuchElementException ex) {
                log.info("Proxy Port: None");
            }
            log.info("User Agent: " + req.getHeaders("User-Agent").nextElement());
            log.info("Accept: " + req.getHeaders("Accept").nextElement());

            resp.setStatus(HttpServletResponse.SC_OK);
            PrintWriter out = resp.getWriter();
            //Sending the port to the load balancer only for testing, not a good practice obviously
            out.println("Healthy BEServer (" + bePort + ")");
            log.info("Responded with: \"Healthy BEServer (" + bePort + ")\"");
            final long endTime = System.currentTimeMillis();
            log.info("Time required to  process the request: " + (endTime - startTime) + " ms");
            log.info("------------------------------------------------------------------------------------------------");
        } catch(Exception e){
            log.error(e.getMessage());
            log.info("Responded with STATUS CODE: "+ HttpURLConnection.HTTP_INTERNAL_ERROR);
            resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

}
