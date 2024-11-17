package com.chapter1.blueprint.member.domain;

public enum PolicyAlarmType {
    RECOMMENDED("RECOMMENDED"),
    MEMBER_DEFINED("MEMBER_DEFINED");

    private final String type;

    PolicyAlarmType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
