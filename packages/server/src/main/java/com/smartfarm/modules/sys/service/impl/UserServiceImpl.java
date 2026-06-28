package com.smartfarm.modules.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.dev33.satoken.stp.StpUtil;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.sys.dto.LoginRequest;
import com.smartfarm.modules.sys.dto.UserCreateRequest;
import com.smartfarm.modules.sys.dto.UserUpdateRequest;
import com.smartfarm.modules.sys.entity.User;
import com.smartfarm.modules.sys.mapper.UserMapper;
import com.smartfarm.modules.sys.service.UserService;
import com.smartfarm.modules.sys.vo.LoginVO;
import com.smartfarm.modules.sys.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Set<String> VALID_ROLES = Set.of("管理员", "农技员");

    private final UserMapper userMapper;

    @Override
    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("LIMIT 1"));
        if (user == null || !user.getPassword().equals(request.getPassword())) {
            throw new BusinessException("Invalid username or password");
        }

        StpUtil.login(user.getUserId());
        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        return vo;
    }

    @Override
    public UserInfoVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "Current user does not exist");
        }

        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public String getRoleByUserId(Long userId) {
        User user = userMapper.selectById(userId);
        return user == null ? null : user.getRole();
    }

    @Override
    public IPage<UserInfoVO> pageUsers(long pageNum, long pageSize, String keyword, String role) {
        validateOptionalRole(role);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .and(StringUtils.hasText(keyword), q -> q
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getRealName, keyword)
                        .or()
                        .like(User::getPhone, keyword))
                .eq(StringUtils.hasText(role), User::getRole, role)
                .orderByDesc(User::getCreatedAt);
        IPage<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return page.convert(this::toUserInfoVO);
    }

    @Override
    public UserInfoVO getUserById(Long userId) {
        return toUserInfoVO(getEntityById(userId));
    }

    @Override
    public void createUser(UserCreateRequest request) {
        validateRole(request.getRole());
        checkDuplicateUsername(null, request.getUsername());
        checkDuplicatePhone(null, request.getPhone());
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone() : null);
        userMapper.insert(user);
    }

    @Override
    public void updateUser(Long userId, UserUpdateRequest request) {
        validateRole(request.getRole());
        User existing = getEntityById(userId);
        ensureAdminRoleChangeSafe(existing, request.getRole());
        checkDuplicateUsername(userId, request.getUsername());
        checkDuplicatePhone(userId, request.getPhone());
        existing.setUsername(request.getUsername());
        existing.setRealName(request.getRealName());
        existing.setRole(request.getRole());
        existing.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone() : null);
        userMapper.updateById(existing);
    }

    @Override
    public void resetPassword(Long userId, String password) {
        User existing = getEntityById(userId);
        existing.setPassword(password);
        userMapper.updateById(existing);
    }

    @Override
    public void deleteUser(Long userId) {
        User existing = getEntityById(userId);
        if (StpUtil.isLogin() && userId.equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("Cannot delete current login user");
        }
        ensureAdminDeleteSafe(existing);
        ensureUserNotReferenced(userId);
        userMapper.deleteById(userId);
    }

    private User getEntityById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "User not found");
        }
        return user;
    }

    private UserInfoVO toUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private void checkDuplicateUsername(Long userId, String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .ne(userId != null, User::getUserId, userId)
                .last("LIMIT 1"));
        if (user != null) {
            throw new BusinessException("Username already exists");
        }
    }

    private void checkDuplicatePhone(Long userId, String phone) {
        if (!StringUtils.hasText(phone)) {
            return;
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone)
                .ne(userId != null, User::getUserId, userId)
                .last("LIMIT 1"));
        if (user != null) {
            throw new BusinessException("Phone already exists");
        }
    }

    private void validateOptionalRole(String role) {
        if (StringUtils.hasText(role)) {
            validateRole(role);
        }
    }

    private void validateRole(String role) {
        if (!VALID_ROLES.contains(role)) {
            throw new BusinessException("Invalid user role");
        }
    }

    private void ensureAdminRoleChangeSafe(User existing, String targetRole) {
        if ("管理员".equals(existing.getRole()) && !"管理员".equals(targetRole) && countAdmins() <= 1) {
            throw new BusinessException("At least one administrator must be retained");
        }
    }

    private void ensureAdminDeleteSafe(User existing) {
        if ("管理员".equals(existing.getRole()) && countAdmins() <= 1) {
            throw new BusinessException("At least one administrator must be retained");
        }
    }

    private long countAdmins() {
        return userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getRole, "管理员"));
    }

    private void ensureUserNotReferenced(Long userId) {
        int references = userMapper.countPlantingPlanByUserId(userId)
                + userMapper.countIrrigationRecordByUserId(userId)
                + userMapper.countFarmLogByUserId(userId)
                + userMapper.countAgriPurchaseRecordByUserId(userId);
        if (references > 0) {
            throw new BusinessException("User is referenced by business records and cannot be deleted");
        }
    }
}
