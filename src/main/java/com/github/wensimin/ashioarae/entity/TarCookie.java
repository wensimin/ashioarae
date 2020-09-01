package com.github.wensimin.ashioarae.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Objects;

/**
 * ashi target 的 cookie
 */
@Entity
public class TarCookie {
    @Id
    @GeneratedValue
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 81920)
    private String value;
    @Column(nullable = false)
    private String domain;
    @Column(nullable = false)
    private String path;
    @JoinColumn(nullable = false)
    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private AshiTarget ashiTarget;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public AshiTarget getAshiTarget() {
        return ashiTarget;
    }

    public void setAshiTarget(AshiTarget ashiTarget) {
        this.ashiTarget = ashiTarget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TarCookie tarCookie = (TarCookie) o;
        return name.equals(tarCookie.name) &&
                value.equals(tarCookie.value) &&
                domain.equals(tarCookie.domain) &&
                path.equals(tarCookie.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, domain, path);
    }
}
