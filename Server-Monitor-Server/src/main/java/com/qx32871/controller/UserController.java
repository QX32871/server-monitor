package com.qx32871.controller;

import com.qx32871.entity.RestBean;
import com.qx32871.entity.vo.request.ChangePasswordVO;
import com.qx32871.entity.vo.request.CreateSubAccountVO;
import com.qx32871.entity.vo.request.ModifyEmailVO;
import com.qx32871.entity.vo.response.SubAccountVO;
import com.qx32871.service.AccountService;
import com.qx32871.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    AccountService accountService;

    @PostMapping("/reset-password")
    public RestBean<Void> resetPassword(@RequestBody @Valid ChangePasswordVO vo,
                                        @RequestAttribute(Const.ATTR_USER_ID) int userId) {
        return accountService.resetPassword(userId, vo.getPassword(), vo.getNew_password()) ?
                RestBean.success() : RestBean.failure(401, "原密码输入错误!");
    }

    @PostMapping("/modify-email")
    public RestBean<Void> modifyEmail(@RequestAttribute(Const.ATTR_USER_ID) int id,
                                      @RequestBody @Valid ModifyEmailVO vo) {
        String result = accountService.modifyEmail(id, vo);
        if (result == null) {
            return RestBean.success();
        } else {
            return RestBean.failure(401, result);
        }
    }

    @PostMapping("/sub/create")
    public RestBean<Void> createSubAccount(@RequestBody @Valid CreateSubAccountVO vo) {
        accountService.createSubAccount(vo);
        return RestBean.success();
    }

    @GetMapping("/sub/delete")
    public RestBean<Void> deleteSubAccount(int uid,
                                           @RequestAttribute(Const.ATTR_USER_ID) int userId) {
        if (uid == userId) {
            return RestBean.failure(401, "非法参数");
        }
        accountService.deleteSubAccount(uid);
        return RestBean.success();
    }

    @GetMapping("/sub/list")
    public RestBean<List<SubAccountVO>> subAccountList() {
        return RestBean.success(accountService.listSubAccount());
    }
}
