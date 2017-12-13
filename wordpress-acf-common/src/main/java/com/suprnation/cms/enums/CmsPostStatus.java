package com.suprnation.cms.enums;

public enum CmsPostStatus {

    publish, future, draft, pending, trash, inherit;


    public static CmsPostStatus forName(String stateName) {
        if (stateName != null) {
            for (CmsPostStatus state : CmsPostStatus.values()) {
                if (state.name().equals(stateName)) {
                    return state;
                }
            }
        }
        return trash;
    }

}
