package com.qx32871.config;

import com.alibaba.fastjson2.JSONObject;
import com.qx32871.entity.ConnectionConfig;
import com.qx32871.utils.MonitorUtils;
import com.qx32871.utils.NetUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Configuration
@Slf4j
public class ServerConfiguration {

    @Resource
    NetUtils netUtils;

    @Resource
    MonitorUtils monitorUtils;

    /**
     * 加载服务端配置，先寻找本地配置文件，没有的话则启动注册
     *
     * @return 服务端配置信息类
     */
    @Bean
    ConnectionConfig connectionConfig() {
        log.info("正在加载服务端配置......");
        ConnectionConfig connectionConfig = this.readConfigurationFromLocalFiles();
        if (connectionConfig == null) {
            connectionConfig = this.registerToServer();
        } else {
            log.info("加载服务端成功!");
        }
        System.out.println(monitorUtils.monitorBaseDetail());
        return connectionConfig;
    }

    /**
     * 向服务端注册操作
     *
     * @return 注册完成后的连接对象
     */
    private ConnectionConfig registerToServer() {
        Scanner in = new Scanner(System.in);
        String address;
        String token;
        do {
            log.info("请输入需要注册的服务端访问地址，地址格式看起来像：'http://192.168.22.44:8080' : ");
            address = in.nextLine();
            log.info("请输入服务端生成的用于客户端注册的Token密钥: ");
            token = in.nextLine();
        } while (!netUtils.registerToServer(address, token));//注册成功就跳出循环，失败就继续注册
        ConnectionConfig config = new ConnectionConfig(address, token);
        this.saveConfigurationToLocalFile(config);
        return config;
    }

    /**
     * 把客户端连接服务端的信息保存到一个本地json文件中
     *
     * @param config 本次连接服务端的连接对象
     */
    private void saveConfigurationToLocalFile(ConnectionConfig config) {
        File dir = new File("config");
        if (dir.exists()) {
            this.saveConfiguration(config);
        } else if (dir.mkdir()) {
            log.info("已创建连接保存目录");
            this.saveConfiguration(config);
        }
    }

    /**
     * 把客户端连接服务端的信息保存到一个本地json文件中
     *
     * @param config 本次连接服务端的连接对象
     */
    private void saveConfiguration(ConnectionConfig config) {
        File file = new File("config/server.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(JSONObject.from(config).toJSONString());
        } catch (IOException e) {
            log.error("保存连接信息失败!", e);
        }
        log.info("连接信息已保存成功!");
    }

    /**
     * 在本地读取已注册的服务器信息
     *
     * @return 连接对象
     */
    private ConnectionConfig readConfigurationFromLocalFiles() {
        File configurationFile = new File("config/server.json");
        if (configurationFile.exists()) {
            try (FileInputStream fileInputStream = new FileInputStream(configurationFile)) {
                String raw = new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);
                return JSONObject.parseObject(raw).to(ConnectionConfig.class);
            } catch (IOException e) {
                log.error("读取配置文件时出错！", e);
            }
        }
        return null;
    }

}
