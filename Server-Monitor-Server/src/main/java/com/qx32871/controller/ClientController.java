package com.qx32871.controller;

import com.qx32871.entity.RestBean;
import com.qx32871.entity.dto.ClientDTO;
import com.qx32871.entity.vo.request.ClientDetailVO;
import com.qx32871.service.ClientService;
import com.qx32871.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    @Resource
    ClientService clientService;

    /**
     * token注册接口
     *
     * @param token 生成的token
     * @return 状态信息
     */
    @GetMapping("/register")
    public RestBean<Void> registerClient(@RequestHeader("Authorization") String token) {
        return clientService.verifyAndRegister(token) ?
                RestBean.success() : RestBean.failure(401, "客户端注册失败，请检查Token是否正确");
    }

    /**
     * @param client 客户端对象
     * @param vo     客户端信息对象
     * @return 状态信息
     */
    @PostMapping("/detail")
    public RestBean<Void> updateClientDetails(@RequestAttribute(Const.ATTR_CLIENT) ClientDTO client,
                                              @RequestBody @Valid ClientDetailVO vo) {
        clientService.updateClientDetail(vo, client);
        return RestBean.success();
    }
}
