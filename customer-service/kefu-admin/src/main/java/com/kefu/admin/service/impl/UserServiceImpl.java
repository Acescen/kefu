package com.kefu.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kefu.admin.common.jwt.JwtTokenUtils;
import com.kefu.admin.common.jwt.JwtUser;
import com.kefu.admin.common.jwt.JwtUserServiceImpl;
import com.kefu.admin.dto.UserDto;
import com.kefu.admin.dto.UserPageDto;
import com.kefu.admin.entity.Permission;
import com.kefu.admin.entity.Role;
import com.kefu.admin.entity.User;
import com.kefu.admin.entity.enums.PermissionTypeEnum;
import com.kefu.admin.mapper.UserMapper;
import com.kefu.admin.service.PermissionService;
import com.kefu.admin.service.RoleService;
import com.kefu.admin.service.TeamService;
import com.kefu.admin.service.UserService;
import com.kefu.admin.vo.ButtonVo;
import com.kefu.admin.vo.MenuVo;
import com.kefu.admin.vo.UserVo;
import com.kefu.common.db.util.PageUtils;
import com.kefu.common.exception.user.UserExistsException;
import com.kefu.common.exception.user.UserNotExistsException;
import com.kefu.common.util.tree.TreeUtils;
import com.kefu.common.vo.PageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * ????????????????????????
 *
 * @author jurui
 * @date 2020-05-18
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUserServiceImpl userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TeamService teamService;

    /**
     * ??????
     *
     * @param user ????????????
     * @return
     * @throws UserExistsException
     */
    @Override
    public void register(User user) throws UserExistsException {

        if (!StringUtils.isEmpty(user.getUsername()) && baseMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getUsername, user.getUsername())) != null) {
            throw new UserExistsException(String.format("???%s??????????????????", user.getUsername()));
        }
        if (!StringUtils.isEmpty(user.getEmail()) && baseMapper.selectOne(new QueryWrapper<User>().lambda().eq(User::getEmail, user.getEmail())) != null) {
            throw new UserExistsException(String.format("???%s??????????????????", user.getEmail()));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // ????????????
        baseMapper.insert(user);

        Role role = roleService.findUserNormalRole();
        if (role == null || role.getId() == null || role.getId() == 0) {
            throw new RuntimeException("?????????????????????");
        }
        log.info("??????????????????,id={}", role.getId());
        List<Integer> roleIds = new ArrayList<>();
        roleIds.add(role.getId());
        // ??????????????????
        updateUserRoleRelation(user.getId(), roleIds);
    }

    /**
     * ??????
     *
     * @param username ??????
     * @param password ??????
     * @return ??????????????????token
     * @throws AuthenticationException
     */
    @Override
    public String login(String username, String password) throws AuthenticationException {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        JwtUser jwtUser = (JwtUser) userDetailsService.loadUserByUsername(username);
        // TODO: ?????????jwtUser??????Redis??????
        return jwtTokenUtils.generateToken(jwtUser);
    }

    /**
     * ??????token
     *
     * @param oldToken ???token
     * @return
     */
    @Override
    public String refreshToken(String oldToken) {
        if (!StringUtils.isEmpty(oldToken)) {
            String token = oldToken.substring(jwtTokenUtils.getTokenHead().length());
            return jwtTokenUtils.refreshToken(token);
        }
        return null;
    }

    /**
     * ????????????????????????
     *
     * @param username ??????
     * @return
     */
    @Override
    public User findByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    /**
     * ???????????????openId????????????
     *
     * @param openId ?????????openId
     * @return
     */
    @Override
    public User findByOpenId(String openId) {
        return baseMapper.selectByOpenId(openId);
    }

    /**
     * ????????????????????????
     *
     * @param username ??????
     * @return
     */
    @Override
    public UserVo findUserInfoByUsername(String username) {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("???%s??????????????????", username));
        }
        UserVo userVo = new UserVo(user);
        List<Permission> permissions = permissionService.findAllByUserId(user.getId());
        log.info("????????????????????????,{}", permissions);
        List<ButtonVo> buttonVos = new ArrayList<>();
        List<MenuVo> menuVos = new ArrayList<>();
        if (permissions != null && permissions.size() > 0) {
            permissions.forEach(permission -> {
                // ????????????
                if (PermissionTypeEnum.BUTTON == permission.getType()) {
                    buttonVos.add(new ButtonVo(
                            permission.getId(),
                            permission.getParentId(),
                            permission.getResources(),
                            permission.getName(),
                            permission.getRemark()
                    ));
                }
                // ????????????
                if (PermissionTypeEnum.MENU == permission.getType()) {
                    menuVos.add(new MenuVo(
                            permission.getId(),
                            permission.getParentId(),
                            permission.getPath(),
                            permission.getComponent(),
                            permission.getResources(),
                            permission.getName(),
                            permission.getIcon(),
                            permission.getSort(),
                            permission.getRemark()
                    ));
                }
            });
        }
        userVo.setButtons(buttonVos);
        userVo.setMenus(TreeUtils.findRoots(menuVos));
        log.info("????????????,{}", userVo);
        return userVo;
    }

    /**
     * ????????????????????????
     *
     * @param userId ????????????
     * @return
     */
    @Override
    public UserVo findUserInfoById(Integer userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new UserNotExistsException("???????????????");
        }
        UserVo userVo = new UserVo(user);
        // ?????????????????????
        userVo.setRoles(roleService.findRoleListByUserId(userId));
        log.info("????????????,{}", userVo);
        return userVo;
    }

    /**
     * ????????????????????????????????????
     *
     * @param userPageDto ????????????
     * @return
     */
    @Override
    public PageVo<User> findUserPageList(UserPageDto userPageDto) {
        IPage<User> page = baseMapper.selectPageList(userPageDto);
        return PageUtils.getPageVo(page);
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     *
     * @param userId  ????????????
     * @param roleIds ??????????????????
     */
    @Override
    public void updateUserRoleRelation(Integer userId, List<Integer> roleIds) {
        // ????????????????????????
        baseMapper.deleteUserRoleByUserId(userId);
        // ????????????
        roleIds = roleIds.stream().sorted().collect(Collectors.toList());
        // ???????????????????????????
        roleIds.forEach(roleId -> baseMapper.insertUserRole(userId, roleId));
    }

    /**
     * ??????????????????
     *
     * @param userDto ????????????
     */
    @Override
    public void updateUser(UserDto userDto) {
        log.info("????????????,{}", JSON.toJSONString(userDto));
        if (userDto.getUserInfo() == null) {
            throw new RuntimeException("??????????????????,user=" + userDto.getUserInfo());
        }
        if (userDto.getUserInfo().getId() == null || userDto.getUserInfo().getId() == 0) {
            throw new RuntimeException("??????????????????,userId=" + userDto.getUserInfo().getId());
        }

        baseMapper.updateById(userDto.getUserInfo());
        if (userDto.getRoleIds() != null && userDto.getRoleIds().size() > 0) {
            updateUserRoleRelation(userDto.getUserInfo().getId(), userDto.getRoleIds());
        }
    }

    /**
     * ????????????
     *
     * @param userDto ????????????
     */
    @Override
    public void addUser(UserDto userDto) {
        log.info("????????????,{}", JSON.toJSONString(userDto));
        if (userDto.getUserInfo() == null) {
            throw new RuntimeException("??????????????????,user=" + userDto.getUserInfo());
        }

        userDto.getUserInfo().setAvatar("avator"+new Random().nextInt(11)+".jpg");
        // ????????????????????????
        userDto.getUserInfo().setPassword(passwordEncoder.encode(userDto.getUserInfo().getPassword()));
        // ??????????????????
        baseMapper.insert(userDto.getUserInfo());
        if (userDto.getUserInfo().getId() == null || userDto.getUserInfo().getId() == 0) {
            throw new RuntimeException("??????????????????,userId=" + userDto.getUserInfo().getId());
        }

        if (userDto.getRoleNameEns() != null && userDto.getRoleNameEns().size() > 0) {
            userDto.setRoleIds(roleService.getRoleIds(userDto.getRoleNameEns()));
        }
        if (userDto.getRoleIds() != null && userDto.getRoleIds().size() > 0) {
            log.info("??????????????????,??????????????????{}", JSON.toJSONString(userDto));
            updateUserRoleRelation(userDto.getUserInfo().getId(), userDto.getRoleIds());
        }
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param roleId ????????????
     */
    @Override
    public void deleteUserRoleRelation(Integer roleId) {
        baseMapper.deleteUserRoleByRoleId(roleId);
    }

    @Override
    public List<User> findAllCustomer() {
        return baseMapper.findAllCustomer();
    }
}
