package com.suprnation.cms.model;

import lombok.*;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;


@Entity
@EqualsAndHashCode
@Getter
@Immutable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "wp_postmeta")
@ToString
public class CmsPostMeta implements Cloneable, Serializable {

    @Id
    @Column(name = "meta_id")
    private long metaId;

    @Column(name = "post_id")
    private long postId;

    @Column(name = "meta_key")
    private String metaKey;

    @Column(name = "meta_value")
    private String  metaValue;

    public long getMetaId() {
        return metaId;
    }

    public long getPostId() {
        return postId;
    }

    public String getMetaKey() {
        return metaKey;
    }

    public String getMetaValue() {
        return metaValue;
    }
}


