package com.jasolar.mis.module.system.oauth;

import java.io.Serializable;

/**
 * Authentication verification request payload.
 */
public class VerifyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;

    private String method;

    public VerifyRequest() {
    }

    public VerifyRequest(String url, String method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String url;
        private String method;

        private Builder() {
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public VerifyRequest build() {
            return new VerifyRequest(url, method);
        }
    }
}
