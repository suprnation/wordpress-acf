package com.suprnation.cms.model;

import lombok.*;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;


@Entity
@EqualsAndHashCode
@Getter
@Immutable
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "wp_term_relationships")
@ToString(of = "pk")
public class CmsTermRelationship implements Cloneable, Serializable {


    @EmbeddedId
    private CmsTermRelationshipPk pk;


}

