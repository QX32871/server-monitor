package com.qx32871.controller;

import com.qx32871.entity.RestBean;
import com.qx32871.entity.vo.request.ChangePasswordVO;
import com.qx32871.service.AccountService;
import com.qx32871.utils.Const;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

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
}
