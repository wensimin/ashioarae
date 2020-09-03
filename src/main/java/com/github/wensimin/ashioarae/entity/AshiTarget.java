package com.github.wensimin.ashioarae.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.wensimin.ashioarae.service.enums.AshiType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    private AshiType type;
    @OneToMany(mappedBy = "ashiTarget", fetch = FetchType.EAGER)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<TarCookie> cookies;
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

    public List<TarCookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<TarCookie> cookies) {
        this.cookies = cookies;
    }

    public SysUser getSysUser() {
        return sysUser;
    }

    public void setSysUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }
}
