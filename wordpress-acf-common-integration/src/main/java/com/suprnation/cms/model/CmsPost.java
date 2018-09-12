package com.suprnation.cms.model;

import com.suprnation.cms.enums.CmsPostStatus;
import com.suprnation.cms.marker.CmsPostIdentifier;
import lombok.*;
import org.hibernate.annotations.Immutable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

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
@Table(name = "wp_posts")
@ToString(of = "id")
public class CmsPost implements Cloneable, Serializable, CmsPostIdentifier {


    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "post_author")
    private long author;

    @Column(name = "post_date_gmt")
    private String postDateGmt;

    @Column(name = "post_content")
    String content;

    @Column(name = "post_title")
    String title;

    @Column(name = "post_status")
    String status;

    @Column(name = "post_name")
    String name;

    @Column(name = "post_type")
    String type;

    @Column(name = "post_modified_gmt")
    private String modifiedGmt;


    @Column(name = "guid")
    private String guid;


    /**
     * To be used by tests only
     * @param id the Post ID
     */
    @Deprecated
    public CmsPost(Long id) {
        this.id = id;
    }

    /**
     * To be used by tests only
     * @param id the Post ID
     */
    @Deprecated
    public CmsPost(Long id, String modifiedGmt, String type, String status) {
        this.id = id;
        this.modifiedGmt = modifiedGmt;
        this.type = type;
        this.status = status;
    }

    public CmsPostStatus getStatus() {
        return CmsPostStatus.forName(this.status);
    }

    @Override
    public Long getWordpressId() {
        return id;
    }

    public Long getId() {
        return id;
    }

    public long getAuthor() {
        return author;
    }

    public DateTime getPostDate() {
        return parseDateTime(postDateGmt);
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public DateTime getModified() {
        return parseDateTime(modifiedGmt);
    }

    public String getGuid() {
        return guid;
    }

    private DateTime parseDateTime(String date) {
        if (date != null)
            return DateTime.parse(date.replace(" ", "T").concat("Z")).withZone(DateTimeZone.UTC);
        else
            return null;
    }
}

