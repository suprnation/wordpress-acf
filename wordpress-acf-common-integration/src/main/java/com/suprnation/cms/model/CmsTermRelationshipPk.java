package com.suprnation.cms.model;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import java.io.Serializable;

@EqualsAndHashCode
@Getter
@Immutable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class CmsTermRelationshipPk implements Serializable {

    @Column(name = "object_id")
    private long postId;
    @Column(name = "term_taxonomy_id")
    private long termId;

}