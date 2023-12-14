package com.jeontongju.authentication.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String msg) {
        super(msg);
    }
}
