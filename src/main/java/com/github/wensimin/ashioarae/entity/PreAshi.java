package com.github.wensimin.ashioarae.entity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
public class PreAshi {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    @NotEmpty
    private String headImage;
    @Column(nullable = false)
    @NotEmpty
    private String nickname;
    @OneToOne
    @JoinColumn(nullable = false)
    private SysUser sysUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
