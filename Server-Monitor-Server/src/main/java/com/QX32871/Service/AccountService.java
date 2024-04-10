package com.QX32871.Service;

import com.QX32871.Entity.DTO.Account;
import com.QX32871.Entity.VO.Request.ConfirmResetVO;
import com.QX32871.Entity.VO.Request.EmailResetVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findAccountByNameOrEmail(String text);

    String registerEmailVerifyCode(String type, String email, String address);

    String resetEmailAccountPassword(EmailResetVO info);

    String resetConfirm(ConfirmResetVO info);
}
