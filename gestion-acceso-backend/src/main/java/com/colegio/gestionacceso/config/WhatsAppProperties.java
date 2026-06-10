package com.colegio.gestionacceso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "whatsapp")
public class WhatsAppProperties {

    private boolean enabled = true;
    private String provider = "bridge";
    private String bridgeUrl = "http://localhost:3001";
    private String bridgeApiKey = "";
    private String businessApiUrl = "";
    private String businessApiToken = "";
    private int maxRetries = 3;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getBridgeUrl() { return bridgeUrl; }
    public void setBridgeUrl(String bridgeUrl) { this.bridgeUrl = bridgeUrl; }

    public String getBridgeApiKey() { return bridgeApiKey; }
    public void setBridgeApiKey(String bridgeApiKey) { this.bridgeApiKey = bridgeApiKey; }

    public String getBusinessApiUrl() { return businessApiUrl; }
    public void setBusinessApiUrl(String businessApiUrl) { this.businessApiUrl = businessApiUrl; }

    public String getBusinessApiToken() { return businessApiToken; }
    public void setBusinessApiToken(String businessApiToken) { this.businessApiToken = businessApiToken; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}
