package com.suprnation.cms.model;

import com.suprnation.cms.annotations.Alias;
import lombok.*;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@EqualsAndHashCode
@Getter
@Immutable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "wp_users")
@ToString(of="id")
public class CmsUser implements Cloneable, Serializable {

    @Id
    @Column(name = "ID")
    private long id;

    @Column(name = "user_login")
    @Alias("user_login")
    private String login;

    @Column(name = "user_nicename")
    @Alias("user_nicename")
    private String niceName;

    @Column(name = "user_email")
    @Alias("user_email")
    private String email;

    @Alias("user_registered")
    @Column(name = "user_registered")
    private ZonedDateTime registered;
}
