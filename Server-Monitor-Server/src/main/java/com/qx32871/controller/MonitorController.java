package com.qx32871.controller;

import com.qx32871.entity.RestBean;
import com.qx32871.entity.dto.AccountDTO;
import com.qx32871.entity.vo.request.RenameClientVO;
import com.qx32871.entity.vo.request.RenameNodeVO;
import com.qx32871.entity.vo.request.RuntimeDetailVO;
import com.qx32871.entity.vo.request.SshConnectionVO;
import com.qx32871.entity.vo.response.*;
import com.qx32871.service.AccountService;
import com.qx32871.service.ClientService;
import com.qx32871.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Resource
    ClientService clientService;

    @Resource
    AccountService accountService;

    @GetMapping("/list")
    public RestBean<List<ClientPreviewVO>> listAllClient(@RequestAttribute(Const.ATTR_USER_ID) int userId,
                                                         @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        List<ClientPreviewVO> clients = clientService.listClients();
        if (this.isAdminAccount(userRole)) {
            return RestBean.success(clients);
        } else {
            List<Integer> ids = this.accountCanAccessClients(userId);
            return RestBean.success(clients.stream()
                    .filter(vo -> ids.contains(vo.getId())).toList());
        }
    }

    @GetMapping("/simple-list")
    public RestBean<List<ClientSimpleVO>> simpleClientList(@RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.isAdminAccount(userRole)) {
            return RestBean.success(clientService.listSimpleList());
        } else {
            return RestBean.noPermission();
        }
    }

    @PostMapping("/rename")
    public RestBean<Void> renameClient(@RequestBody @Valid RenameClientVO vo,
                                       @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                       @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, vo.getId())) {
            clientService.renameClient(vo);
            return RestBean.success();
        } else {
            return RestBean.noPermission();
        }

    }

    @GetMapping("/details")
    public RestBean<ClientDetailsVO> details(int clientId,
                                             @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                             @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, clientId)) {
            return RestBean.success(clientService.clientDetails(clientId));
        } else {
            return RestBean.noPermission();
        }
    }

    @PostMapping("/node")
    public RestBean<Void> renameNode(@RequestBody @Valid RenameNodeVO vo,
                                     @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                     @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, vo.getId())) {
            clientService.renameNode(vo);
            return RestBean.success();
        } else {
            return RestBean.noPermission();
        }

    }

    @GetMapping("/runtime-history")
    public RestBean<RuntimeHistoryVO> runtimeDetailsHistory(int clientId,
                                                            @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                                            @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, clientId)) {
            return RestBean.success(clientService.clientRuntimeHistoryDetails(clientId));
        } else {
            return RestBean.noPermission();
        }
    }

    @GetMapping("/runtime-now")
    public RestBean<RuntimeDetailVO> runtimeDetailsNow(int clientId,
                                                       @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                                       @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, clientId)) {
            return RestBean.success(clientService.clientRuntimeDetailsNow(clientId));
        } else {
            return RestBean.noPermission();
        }
    }

    @GetMapping("/register")
    public RestBean<String> registerToken(@RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.isAdminAccount(userRole)) {
            return RestBean.success(clientService.registerToken());
        } else {
            return RestBean.noPermission();
        }
    }

    @GetMapping("/delete")
    public RestBean<String> deleteClient(int clientId,
                                         @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.isAdminAccount(userRole)) {
            clientService.deleteClient(clientId);
            return RestBean.success();
        } else {
            return RestBean.noPermission();
        }

    }

    @PostMapping("/ssh-save")
    public RestBean<Void> saveSshConnection(@RequestBody @Valid SshConnectionVO vo,
                                            @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                            @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, vo.getId())) {
            clientService.saveSshClientConnection(vo);
            return RestBean.success();
        } else {
            return RestBean.noPermission();
        }
    }

    @GetMapping("/ssh")
    public RestBean<SshSettingsVO> sshSettings(int clientId,
                                               @RequestAttribute(Const.ATTR_USER_ID) int userId,
                                               @RequestAttribute(Const.ATTR_USER_ROLE) String userRole) {
        if (this.permissionCheck(userId, userRole, clientId)) {
            return RestBean.success(clientService.sshSettings(clientId));
        } else {
            return RestBean.noPermission();
        }
    }

    /**
     * 取出用户可以访问的实例主机
     *
     * @param uid 用户id
     * @return 可访问的实例主机列表
     */
    private List<Integer> accountCanAccessClients(int uid) {
        AccountDTO account = accountService.getById(uid);
        return account.getClientList();
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @param role 用户角色
     * @return 是否为管理员
     */
    private boolean isAdminAccount(String role) {
        role = role.substring(5);
        return Const.ROLE_ADMIN.equals(role);
    }

    /**
     * 检查当前用户是否有这个客户端的权限，如果是管理员则直接放行，如果不是管理员则检查当前账户是否有可访问的实例主机
     *
     * @param uid      用户id
     * @param role     用户权限
     * @param clientId 客户端id
     * @return 是否具有权限和是否有可访问的实例主机
     */
    private boolean permissionCheck(int uid, String role, int clientId) {
        if (this.isAdminAccount(role)) {
            return true;
        }
        return this.accountCanAccessClients(uid).contains(clientId);
    }
}
