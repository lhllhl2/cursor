package com.jasolar.mis.module.system.oauth;

import java.io.Serializable;

/**
 * Client credentials metadata for outer system integrations.
 */
public class ClientInfoResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clientName;

    private String clientSecret;

    public ClientInfoResponse() {
    }

    public ClientInfoResponse(String clientName, String clientSecret) {
        this.clientName = clientName;
        this.clientSecret = clientSecret;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
