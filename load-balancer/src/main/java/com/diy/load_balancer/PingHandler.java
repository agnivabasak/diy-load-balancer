package com.diy.load_balancer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.URL;

public class PingHandler extends HttpServlet {
    private LBAlgo lbAlgo;
    private int lbPort;
    private final Logger log = LogManager.getLogger(PingHandler.class);

    @Override
    public void init() throws ServletException
    {
        this.lbAlgo = (LBAlgo) getServletContext().getAttribute("lb.lbAlgo");
        this.lbPort = (int) getServletContext().getAttribute("lb.port");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final long startTime = System.currentTimeMillis();
            log.info("------------------------------------------------------------------------------------------------");
            String ipAddress = req.getHeader("X-Forwarded-For");
            if (ipAddress == null) {
                ipAddress = req.getRemoteAddr();
            }
            ipAddress = ipAddress.contains(",") ? ipAddress.split(",")[0] : ipAddress;
            log.info("LBServer - /ping");
            log.info("Received request from " + ipAddress);
            log.info(req.getMethod() + " / " + req.getProtocol());
            log.info("Host: " + req.getHeaders("Host").nextElement());
            log.info("User Agent: " + req.getHeaders("User-Agent").nextElement());
            log.info("Accept: " + req.getHeaders("Accept").nextElement());

            String backendUrl = lbAlgo.getNextServerUrl();
            if(backendUrl==null){
                log.error("No backend server available to forward the request to.");
                resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
                return;
            }

            URL obj = new URL(backendUrl+"/ping");
            log.info("Forwarding request to : " + backendUrl);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod(req.getMethod());
            //Setting Proxy properties/headers for the request
            //The loopback address is what is coming in getRemoteAddr() in the backend server
            //The behaviour might be different in case the lb is hosted somewhere other than localhost
            //In that case we might have to pass the IP as part of the arguments/VM Options
            con.setRequestProperty("X-Forwarded-For", ipAddress + ", " + Inet4Address.getLoopbackAddress().toString().split("/")[1]);
            con.setRequestProperty("X-Forwarded-Host", "localhost:" + this.lbPort);
            con.setRequestProperty("X-Forwarded-Port", String.valueOf(this.lbPort));
            con.setRequestProperty("X-Forwarded-Proto", req.getProtocol().split("/")[0].toLowerCase());

            int responseCode = con.getResponseCode();
            log.info("GET Response Code from Backend Server: " + responseCode);

            StringBuilder response = new StringBuilder();
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            } else {
                log.error("GET request did not work.");
            }

            resp.setStatus(responseCode);
            PrintWriter out = resp.getWriter();
            out.println(response);
            log.info("Backend server responded with: \"" + response + "\"");
            final long endTime = System.currentTimeMillis();
            log.info("Time required to  process the request: " + (endTime - startTime) + " ms");
            log.info("------------------------------------------------------------------------------------------------");
        } catch(Exception e){
            log.error(e.getMessage());
            log.info("Responded with STATUS CODE: "+HttpURLConnection.HTTP_INTERNAL_ERROR);
            resp.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

}
