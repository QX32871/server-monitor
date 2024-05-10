package com.qx32871.utils;

import com.alibaba.fastjson2.JSONObject;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import com.qx32871.entity.dto.RuntimeDataDTO;
import com.qx32871.entity.vo.request.RuntimeDetailVO;
import com.qx32871.entity.vo.response.RuntimeHistoryVO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

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

    /**
     * 把运行时数据写入influxDB中
     *
     * @param clientId 实例主机客户端ID
     * @param vo       包含运行时数据的对象
     */
    public void writeRuntimeData(int clientId, RuntimeDetailVO vo) {
        RuntimeDataDTO data = new RuntimeDataDTO();
        BeanUtils.copyProperties(vo, data);
        data.setTimestamp(new Date(vo.getTimesTamp()).toInstant());
        data.setClientId(clientId);
        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writeMeasurement(BUCKET, ORG, WritePrecision.NS, data);
    }

    public RuntimeHistoryVO readRuntimeData(int clientId) {
        RuntimeHistoryVO vo = new RuntimeHistoryVO();
        String query = """
                from(bucket: "%s")
                |> range(start: %s)
                |> filter(fn: (r) => r["_measurement"] == "runtime")
                |> filter(fn: (r) => r["clientId"] == "%s")
                """;
        String format = String.format(query, BUCKET, "-1h", clientId);
        List<FluxTable> tables = client.getQueryApi().query(format, ORG);
        int size = tables.size();
        if (size == 0) {
            return vo;
        }
        List<FluxRecord> records = tables.get(0).getRecords();
        for (int i = 0; i < records.size(); i++) {
            JSONObject object = new JSONObject();
            object.put("timestamp", records.get(i).getTime());
            for (int j = 0; j < size; j++) {
                FluxRecord record = tables.get(j).getRecords().get(i);
                object.put(record.getField(), record.getValue());
            }
            vo.getList().add(object);
        }
        return vo;
    }


}
