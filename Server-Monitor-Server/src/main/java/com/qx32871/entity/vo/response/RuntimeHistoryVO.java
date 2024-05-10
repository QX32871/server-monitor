package com.qx32871.entity.vo.response;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class RuntimeHistoryVO {
    private double disk;
    private double memory;
    List<JSONObject> list = new LinkedList<>();
}
