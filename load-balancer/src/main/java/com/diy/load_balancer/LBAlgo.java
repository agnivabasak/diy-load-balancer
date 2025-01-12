package com.diy.load_balancer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LBAlgo {
    private static final Logger log = LogManager.getLogger(LBServer.class);
    //Does the actual processing of which backend server to choose from the pool of the servers
    //Since this is a simple load balancer, we are not utilizing layer 7 information to route into different backend servers
    //i.e, we are assuming every backend server can server all the requests.

    private final LBAlgoEnum selectedAlgo;

    //Replaced ArrayList with CopyOnWriteArrayList for thread safety
    //To ensure when we do health checks and remove/add any servers to the list it
    //should happen in a thread safe way
    private final CopyOnWriteArrayList<String> backendDetails;

    private final CopyOnWriteArrayList<String> unhealthyBackendDetails;

    private int currentBackendServer;

    public LBAlgo(LBAlgoEnum selectedAlgo, String backendServers, Integer healthCheckInterval){
        this.selectedAlgo = selectedAlgo;
        this.backendDetails = new CopyOnWriteArrayList<>();
        this.unhealthyBackendDetails = new CopyOnWriteArrayList<>();
        this.currentBackendServer = -1;
        Arrays.stream(backendServers.split(",")).map(String::strip).forEach(
                backendDetails::add
        );
        if(backendDetails.isEmpty()) backendDetails.add("http://localhost:8081");
        int healthCheckInterval1 = healthCheckInterval;

        try{
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    performHealthCheck();
                }
            }, 0, healthCheckInterval1, TimeUnit.SECONDS);
        } catch (Exception e){
            log.error("Error in starting health check thread!");
            log.error(e.getMessage());
        }

    }

    synchronized public String getNextServerUrl(){
        if(backendDetails.isEmpty()){
            return null;
        }

        if(selectedAlgo==LBAlgoEnum.ROUND_ROBIN){
            currentBackendServer = (currentBackendServer+1) % (backendDetails.size());
            return backendDetails.get(currentBackendServer);
        } else{
            return "http://localhost:8081";
        }
    }

    synchronized private void performHealthCheck(){

        log.info("PERFORMING HEALTH CHECK");

        //Remove unhealthy backend servers and put it in unhealthyBackendDetails
        int changeCurrentBackendServer = 0;
        List<String> becameUnhealthy = new ArrayList<>();
        for(int i=0;i<backendDetails.size();i++){
            String url = backendDetails.get(i);
            URL obj = null;
            try {
                obj = new URL(url+"/health");
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                int responseCode = con.getResponseCode();
                if(responseCode!=200){
                    becameUnhealthy.add(url);
                    if(currentBackendServer>=i) changeCurrentBackendServer++;
                    log.info("Identified unhealthy server - "+url);
                }
            } catch (Exception e) {
                log.error("Error while trying to do a health check for "+url);
                log.error(e.getMessage());
                becameUnhealthy.add(url);
                if(currentBackendServer>=i) changeCurrentBackendServer++;
                log.info("Identified unhealthy server - "+url);
            }
        }

        //Remove healthy backend servers and put it in healthyBackendDetails
        List<String> becameHealthy = new ArrayList<>();
        unhealthyBackendDetails.forEach((String url)->{
            URL obj = null;
            try {
                obj = new URL(url+"/health");
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("GET");
                int responseCode = con.getResponseCode();
                if(responseCode==200){
                    becameHealthy.add(url);
                    log.info("Identified healthy server - "+url);
                }
            } catch (Exception e) {
                log.error("Error while trying to do a health check for "+url);
                log.error(e.getMessage());
            }
        });

        backendDetails.removeAll(becameUnhealthy);
        backendDetails.addAll(becameHealthy);
        unhealthyBackendDetails.removeAll(becameHealthy);
        unhealthyBackendDetails.addAll(becameUnhealthy);
        currentBackendServer-=changeCurrentBackendServer;
        log.info("HEALTH CHECK COMPLETE");
    }

}
