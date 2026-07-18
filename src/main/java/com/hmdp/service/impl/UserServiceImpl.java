package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result senCode(String phone, HttpSession session) {
        //校验手机号是否符合规范
        if(RegexUtils.isPhoneInvalid(phone)){
            //返回错误信息
            return Result.fail("手机号格式不正确");
        }
        //生成验证码
        String code = RandomUtil.randomNumbers(6);
        //保存验证码到session
        session.setAttribute("code",code);
        //发送验证码
        log.debug("发送成功！验证码为：{}",code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.校验手机号
        String phone=loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            //返回错误信息
            return Result.fail("手机号格式不正确");
        }
        // 2.通过session获取验证码
            String code = session.getAttribute("code").toString();
        // 3.校验验证码是否正确
        if (loginForm.getCode() == null || !loginForm.getCode().equals(code)) {
            return Result.fail("验证码错误！");
        }
        // 4.查询用户是否存在
        User user = query().eq("phone", phone).one();
        if (user == null) {
            // 5.用户为空，创建新用户
            user=createUserWithPhone(phone);
        }
        // 6.保存用户到session
        session.setAttribute("user",user);
        return Result.ok();
    }

    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX+RandomUtil.randomString(10));
        // 2.将用户保存到数据库
        save(user);
        return user;
    }
}
