package com.suprnation.cms.dto;

import com.suprnation.cms.enums.CmsPostStatus;
import com.suprnation.cms.marker.CmsPostIdentifier;

public interface CmsBaseDto extends CmsPostIdentifier {

    CmsPostStatus getStatus();
}
