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
@Table(name = "wp_terms")
@ToString
public class CmsTerm implements Cloneable, Serializable {


    @Id
    @Column(name = "term_id")
    private Long termId;

    private String slug;

}

