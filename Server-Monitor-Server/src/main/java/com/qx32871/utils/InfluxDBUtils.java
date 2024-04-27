package com.qx32871.utils;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.qx32871.entity.dto.RuntimeDataDTO;
import com.qx32871.entity.vo.request.RuntimeDetailVO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class InfluxDBUtils {

    @Value("${spring.influx.url}")
    private String url;

    @Value("${spring.influx.user}")
    private String username;

    @Value("${spring.influx.password}")
    private String password;

    private final String BUCKET = "monitor";

    private final String ORG = "ORG_INIT";

    private InfluxDBClient client;

    @PostConstruct
    public void init() {
        client = InfluxDBClientFactory.create(url, username, password.toCharArray());
    }

    public void writeRuntimeData(int clientId, RuntimeDetailVO vo) {
        RuntimeDataDTO data = new RuntimeDataDTO();
        BeanUtils.copyProperties(vo, data);
        data.setTimestamp(new Date(vo.getTimesTamp()).toInstant());
        data.setClientId(clientId);
        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writeMeasurement(BUCKET, ORG, WritePrecision.NS, data);

    }
}
