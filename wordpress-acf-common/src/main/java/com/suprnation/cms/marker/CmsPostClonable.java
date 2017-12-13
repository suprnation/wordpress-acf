package com.suprnation.cms.marker;

import java.io.Serializable;

public interface CmsPostClonable extends Serializable {
    CmsPostClonable cloneObject();
}
