package com.github.wensimin.ashioarae.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.wensimin.ashioarae.service.enums.AshiType;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * 更新目标实体
 */
@Entity
public class AshiTarget {
    @Id
    @GeneratedValue
    private Long id;
    @NotNull
    @Column(nullable = false)
    private AshiType type;
    private String headImage;
    private String nickname;
    @NotEmpty
    @Column(nullable = false, length = 81920)
    private String cookie;
    @JoinColumn(nullable = false)
    @ManyToOne
    @JsonIgnore
    private SysUser sysUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AshiType getType() {
        return type;
    }

    public void setType(AshiType type) {
        this.type = type;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public SysUser getSysUser() {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }
}
