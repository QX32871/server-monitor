package com.qx32871.utils;

import com.alibaba.fastjson2.JSONObject;
import com.qx32871.entity.ConnectionConfig;
import com.qx32871.entity.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Slf4j
public class NetUtils {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Lazy   //防止ConnectionConfig这个bean没有注册成功的时候被引用，设置懒加载 防止循环引用
    @Resource
    ConnectionConfig connectionConfig;

    /**
     * 向服务端注册
     *
     * @param address 服务端地址
     * @param token   用于注册客户端的token
     * @return 注册状态对象
     */
    public boolean registerToServer(String address, String token) {
        log.info("正在向服务端注册Token......");
        Response response = this.doGet("/register", address, token);
        if (response.success()) {
            log.info("客户端注册已完成!");
        } else {
            log.error("客户端注册失败:{}", response.message());
        }
        return response.success();
    }

    //重载方法
    private Response doGet(String url) {
        return this.doGet(url, connectionConfig.getAddress(), connectionConfig.getToken());
    }

    /**
     * 向服务端发起Http请求注册Token
     *
     * @param url     访问服务端注册api的url
     * @param address 服务端地址
     * @param token   用于注册客户端的token
     * @return 注册状态对象
     */
    private Response doGet(String url, String address, String token) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .GET().uri(new URI(address + "/api/client" + url))
                    .header("Authorization", token)
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return JSONObject.parseObject(response.body()).to(Response.class);
        } catch (Exception e) {
            log.error("在发起服务端请求时出现问题！", e);
            return Response.errorResponse(e);
        }
    }
}
