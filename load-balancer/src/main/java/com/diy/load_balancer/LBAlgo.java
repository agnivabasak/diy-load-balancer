package com.diy.load_balancer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LBAlgo {
    //Does the actual processing of which backend server to choose from the pool of the servers
    //Since this is a simple load balancer, we are not utilizing layer 7 information to route into different backend servers
    //i.e, we are assuming every backend server can server all the requests.

    private final LBAlgoEnum selectedAlgo;

    private final List<BackendDetail> backendDetails;

    private int currentBackendServer;

    public LBAlgo(LBAlgoEnum selectedAlgo, String backendServers){
        this.selectedAlgo = selectedAlgo;
        this.backendDetails = new ArrayList<>();
        this.currentBackendServer = -1;
        List<String> backendUrls = Arrays.stream(backendServers.split(",")).map(String::strip).toList();
        backendUrls.forEach((String beUrl)->{
           backendDetails.add(new BackendDetail(beUrl, BackendStatusEnum.HEALTHY));
        });
        if(backendDetails.isEmpty()) backendDetails.add(new BackendDetail("http://localhost:8081",BackendStatusEnum.HEALTHY));
    }

    synchronized public String getNextServerUrl(){
        if(selectedAlgo==LBAlgoEnum.ROUND_ROBIN){
            currentBackendServer = (currentBackendServer+1) % (backendDetails.size());
            return backendDetails.get(currentBackendServer).getBackendUrl();
        } else{
            return "http://localhost:8081";
        }
    }

}
