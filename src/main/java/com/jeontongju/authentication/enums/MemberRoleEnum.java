package com.jeontongju.authentication.enums;

public enum MemberRoleEnum {
    ROLE_CONSUMER("ROLE_CONSUMER"),
    ROLE_SELLER("ROLE_SELLER"),
    ROLE_ADMIN("ROLE_ADMIN");

    private final String value;

    private MemberRoleEnum(String value) { this.value = value; }
}
