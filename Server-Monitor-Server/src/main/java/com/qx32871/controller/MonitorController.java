package com.qx32871.controller;

import com.qx32871.entity.RestBean;
import com.qx32871.entity.vo.request.RenameClientVO;
import com.qx32871.entity.vo.request.RenameNodeVO;
import com.qx32871.entity.vo.request.RuntimeDetailVO;
import com.qx32871.entity.vo.response.ClientDetailsVO;
import com.qx32871.entity.vo.response.ClientPreviewVO;
import com.qx32871.entity.vo.response.RuntimeHistoryVO;
import com.qx32871.service.ClientService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Resource
    ClientService clientService;

    @GetMapping("/list")
    public RestBean<List<ClientPreviewVO>> listAllClient() {
        return RestBean.success(clientService.listClients());
    }

    @PostMapping("/rename")
    public RestBean<Void> renameClient(@RequestBody @Valid RenameClientVO vo) {
        clientService.renameClient(vo);
        return RestBean.success();
    }

    @GetMapping("/details")
    public RestBean<ClientDetailsVO> details(int clientId) {
        return RestBean.success(clientService.clientDetails(clientId));
    }

    @PostMapping("/node")
    public RestBean<Void> renameNode(@RequestBody @Valid RenameNodeVO vo) {
        clientService.renameNode(vo);
        return RestBean.success();
    }

    @GetMapping("/runtime-history")
    public RestBean<RuntimeHistoryVO> runtimeDetailsHistory(int clientId) {
        return RestBean.success(clientService.clientRuntimeHistoryDetails(clientId));
    }

    @GetMapping("/runtime-now")
    public RestBean<RuntimeDetailVO> runtimeDetailsNow(int clientId) {
        return RestBean.success(clientService.clientRuntimeDetailsNow(clientId));
    }

    @GetMapping("/register")
    public RestBean<String> registerToken() {
        return RestBean.success(clientService.registerToken());
    }

    @GetMapping("/delete")
    public RestBean<String> deleteClient(int clientId) {
        clientService.deleteClient(clientId);
        return RestBean.success();
    }
}
