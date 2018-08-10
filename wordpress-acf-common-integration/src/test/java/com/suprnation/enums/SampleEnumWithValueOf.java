package com.suprnation.enums;

public enum SampleEnumWithValueOf {
    NORMAL,
    TEST;

    public static SampleEnumWithValueOf getEnum(String value) {
        for (SampleEnumWithValueOf re : SampleEnumWithValueOf.values()) {
            if (re.name().compareTo(value) == 0) {
                return re;
            }
        }
        return null;
    }


}
