package com.qx32871.service;

import com.qx32871.entity.dto.AccountDTO;
import com.qx32871.entity.vo.request.ConfirmResetVO;
import com.qx32871.entity.vo.request.EmailResetVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<AccountDTO>, UserDetailsService {
    AccountDTO findAccountByNameOrEmail(String text);

    String registerEmailVerifyCode(String type, String email, String address);

    String resetEmailAccountPassword(EmailResetVO info);

    String resetConfirm(ConfirmResetVO info);
}
