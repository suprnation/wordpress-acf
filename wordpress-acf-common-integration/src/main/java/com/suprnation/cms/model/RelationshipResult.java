package com.suprnation.cms.model;

import org.springframework.beans.factory.annotation.Value;

public interface RelationshipResult {
    @Value("#{target.postId}")
    Long getPostId();

    @Value("#{target.terms}")
    String getTerms();

    @Value("#{target.taxonomy}")
    String getTaxonomy();

}
