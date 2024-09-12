package com.github.altfatterz.vaultpostgresqlr2dbcdemo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("db")
public class DatabaseConnectionProperties {

    private String username;

    private String password;

    private String url;

    public String getHost() {
        return url.substring(url.indexOf("//") + 2, url.lastIndexOf(":"));
    }

    public Integer getPort() {
        return Integer.valueOf(url.substring(url.lastIndexOf(":") + 1, url.lastIndexOf("/")));
    }

    public String getDatabase() {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
