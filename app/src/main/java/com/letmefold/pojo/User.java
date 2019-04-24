package com.letmefold.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author SuccessZhang
 * @date 2019/01/03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 自增主键
     */
    private Long aid;

    /**
     * 用户id
     */
    private String id;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 腾讯openid可唯一标识一个QQ用户
     */
    private String openid;

    /**
     * 新浪微博uid可唯一标识一个微博用户
     */
    private String uid;

    /**
     * 百度人脸用户id
     */
    private String faceId;

    /**
     * 百度人脸用户组
     */
    private String faceGroup;

    /**
     * 用户名
     */
    private String name;

    /**
     * 性别
     */
    private String gender;

    /**
     * 用户类型
     */
    private String type;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 会员到期时间
     */
    private Date membershipExpireTime;

    /**
     * 最近一次登录IP
     */
    private String lastLoginIp;

    /**
     * 最近一次登录日期
     */
    private Date lastLoginDate;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 1逻辑删除，0不删除
     */
    private Boolean dr;
}