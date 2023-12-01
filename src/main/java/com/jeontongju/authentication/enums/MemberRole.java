package com.jeontongju.authentication.enums;

public enum MemberRole {
    ROLE_USER("ROLE_USER"),
    ROLE_MANAGER("ROLE_MANAGER"),
    ROLE_ADMIN("ROLE_ADMIN");

    private final String value;

    private MemberRole(String value) { this.value = value; }
}
