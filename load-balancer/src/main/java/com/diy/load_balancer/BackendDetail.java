package com.diy.load_balancer;

public class BackendDetail {
    private String backendUrl;
    private BackendStatusEnum healthStatus;

    public BackendDetail(String backendUrl, BackendStatusEnum healthStatus){
        this.backendUrl = backendUrl;
        this.healthStatus = healthStatus;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public BackendStatusEnum getHealthStatus() {
        return healthStatus;
    }

    public void setBackendUrl(String backendUrl) {
        this.backendUrl = backendUrl;
    }

    public void setHealthStatus(BackendStatusEnum healthStatus) {
        this.healthStatus = healthStatus;
    }
}
