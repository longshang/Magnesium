package net.onebean.api.adapter.vo;

public class GetAccessTokenResponse {

    public GetAccessTokenResponse (String appId,String accessToken,String expireIn){
        this.appId = appId;
        this.accessToken = accessToken;
        this.expireIn = expireIn;
    }

    private String accessToken;
    private String expireIn;
    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(String expireIn) {
        this.expireIn = expireIn;
    }

    public GetAccessTokenResponse() {
    }
}
