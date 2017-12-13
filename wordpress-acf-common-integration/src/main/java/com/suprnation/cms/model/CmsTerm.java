package com.suprnation.cms.model;

import lombok.*;
import org.hibernate.annotations.Immutable;

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
@Table(name = "wp_terms")
@ToString
public class CmsTerm implements Cloneable, Serializable {


    @Id
    @Column(name = "term_id")
    private Long termId;

    private String slug;

}

