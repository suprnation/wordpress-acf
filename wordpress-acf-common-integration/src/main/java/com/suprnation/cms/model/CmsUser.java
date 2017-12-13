package com.suprnation.cms.model;

import com.suprnation.cms.annotations.Alias;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

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
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime registered;
}
