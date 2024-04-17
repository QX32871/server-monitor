package com.qx32871;

import com.qx32871.utils.MonitorUtils;
import com.qx32871.utils.NetUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class ServerMonitorClientApplication implements ApplicationRunner {

    @Resource
    private NetUtils netUtils;

    @Resource
    private MonitorUtils monitorUtils;

    public static void main(String[] args) {
        SpringApplication.run(ServerMonitorClientApplication.class, args);
    }

    //在客户端运行时向服务端更新基本信息
    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("正在向服务端更新基本信息......");
            netUtils.updateBaseDetails(monitorUtils.monitorBaseDetail());
        } catch (Exception e) {
            log.error("ERROR: ", e);
        }
    }
}
