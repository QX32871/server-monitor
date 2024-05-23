package com.qx32871.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qx32871.entity.dto.AccountDTO;
import com.qx32871.entity.vo.request.ConfirmResetVO;
import com.qx32871.entity.vo.request.CreateSubAccountVO;
import com.qx32871.entity.vo.request.EmailResetVO;
import com.qx32871.entity.vo.request.ModifyEmailVO;
import com.qx32871.entity.vo.response.SubAccountVO;
import com.qx32871.mapper.AccountMapper;
import com.qx32871.service.AccountService;
import com.qx32871.utils.Const;
import com.qx32871.utils.FlowUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 账户信息处理相关服务
 */
@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, AccountDTO> implements AccountService {

    //验证邮件发送冷却时间限制，秒为单位
    @Value("${spring.web.verify.mail-limit}")
    private int verifyLimit;

    @Resource
    private AmqpTemplate rabbitTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private FlowUtils flow;

    /**
     * 从数据库中通过用户名或邮箱查找用户详细信息
     *
     * @param username 用户名
     * @return 用户详细信息
     * @throws UsernameNotFoundException 如果用户未找到则抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountDTO account = this.findAccountByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    /**
     * 生成注册验证码存入Redis中，并将邮件发送请求提交到消息队列等待发送
     *
     * @param type    类型
     * @param email   邮件地址
     * @param address 请求IP地址
     * @return 操作结果，null表示正常，否则为错误原因
     */
    public String registerEmailVerifyCode(String type, String email, String address) {
        synchronized (address.intern()) {
            if (!this.verifyLimit(address))
                return "请求频繁，请稍后再试";
            Random random = new Random();
            int code = random.nextInt(899999) + 100000;
            Map<String, Object> data = Map.of("type", type, "email", email, "code", code);
            rabbitTemplate.convertAndSend(Const.MQ_MAIL, data);
            stringRedisTemplate.opsForValue()
                    .set(Const.VERIFY_EMAIL_DATA + email, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        }
    }

    /**
     * 邮件验证码重置密码操作，需要检查验证码是否正确
     *
     * @param info 重置基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    @Override
    public String resetEmailAccountPassword(EmailResetVO info) {
        String verify = resetConfirm(new ConfirmResetVO(info.getEmail(), info.getCode()));
        if (verify != null) {
            return verify;
        }
        String email = info.getEmail();
        String password = passwordEncoder.encode(info.getPassword());
        boolean update = this.update().eq("email", email).set("password", password).update();
        if (update) {
            this.deleteEmailVerifyCode(email);
        }
        return update ? null : "更新失败，请联系管理员";
    }

    /**
     * 重置密码确认操作，验证验证码是否正确
     *
     * @param info 验证基本信息
     * @return 操作结果，null表示正常，否则为错误原因
     */
    @Override
    public String resetConfirm(ConfirmResetVO info) {
        String email = info.getEmail();
        String code = this.getEmailVerifyCode(email);
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(info.getCode())) {
            return "验证码错误，请重新输入";
        }
        return null;
    }

    /**
     * 更改密码操作
     *
     * @param id             用户ID
     * @param passwordBefore 旧密码
     * @param passwordNew    新密码
     * @return 是否成功
     */
    @Override
    public boolean resetPassword(int id, String passwordBefore, String passwordNew) {
        AccountDTO account = this.getById(id);
        String password = account.getPassword();
        if (!passwordEncoder.matches(passwordBefore, password)) {
            return false;
        }
        this.update(Wrappers.<AccountDTO>update().eq("id", id)
                .set("password", passwordEncoder.encode(passwordNew)));
        return true;
    }

    /**
     * 创建子账户
     *
     * @param vo 子账户创建信息
     */
    @Override
    public void createSubAccount(CreateSubAccountVO vo) {
        AccountDTO account = this.findAccountByNameOrEmail(vo.getEmail());
        if (account != null) {
            throw new IllegalArgumentException("该电子邮件已被注册!");
        }
        account = this.findAccountByNameOrEmail(vo.getUsername());
        if (account != null) {
            throw new IllegalArgumentException("该用户名已被注册!");
        }
        account = new AccountDTO(null, vo.getUsername(), passwordEncoder.encode(vo.getPassword())
                , vo.getEmail(), Const.ROLE_NORMAL, new Date(), JSONArray.copyOf(vo.getClients()).toJSONString());
        this.save(account);
    }

    /**
     * 删除子账户
     *
     * @param uid 子账户ID
     */
    @Override
    public void deleteSubAccount(int uid) {
        this.removeById(uid);
    }

    /**
     * 获取子账户列表
     *
     * @return 子账户列表
     */
    @Override
    public List<SubAccountVO> listSubAccount() {
        return this.list(Wrappers.<AccountDTO>query().eq("role", Const.ROLE_NORMAL))
                .stream().map(accountDTO -> {
                    SubAccountVO vo = accountDTO.asViewObject(SubAccountVO.class);  //把DTO转成VO
                    vo.setClientList(JSONArray.parse(accountDTO.getClients()));
                    return vo;  //再把转换完成的vo返回到流中去
                }).toList(); //最后把流再转回List并返回
    }

    /**
     * 修改用户邮箱地址
     *
     * @param id 用户ID
     * @param vo 修改用户邮箱地址信息
     * @return 提示信息
     */
    @Override
    public String modifyEmail(int id, ModifyEmailVO vo) {
        String code = getEmailVerifyCode(vo.getEmail());
        if (code == null) {
            return "请先获取验证码";
        }
        if (!code.equals(vo.getCode())) {
            return "验证码错误";
        }
        this.deleteEmailVerifyCode(vo.getEmail());
        AccountDTO account = this.findAccountByNameOrEmail(vo.getEmail());
        if (account == null || account.getId() != id) {
            return "改邮箱已被绑定";
        }
        this.update()
                .set("email", vo.getEmail())
                .eq("id", id)
                .update();
        return null;
    }

    /**
     * 移除Redis中存储的邮件验证码
     *
     * @param email 电邮
     */
    private void deleteEmailVerifyCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA + email;
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取Redis中存储的邮件验证码
     *
     * @param email 电邮
     * @return 验证码
     */
    private String getEmailVerifyCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA + email;
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 针对IP地址进行邮件验证码获取限流
     *
     * @param address 地址
     * @return 是否通过验证
     */
    private boolean verifyLimit(String address) {
        String key = Const.VERIFY_EMAIL_LIMIT + address;
        return flow.limitOnceCheck(key, verifyLimit);
    }

    /**
     * 通过用户名或邮件地址查找用户
     *
     * @param text 用户名或邮件
     * @return 账户实体
     */
    public AccountDTO findAccountByNameOrEmail(String text) {
        return this.query()
                .eq("username", text).or()
                .eq("email", text)
                .one();
    }

}
