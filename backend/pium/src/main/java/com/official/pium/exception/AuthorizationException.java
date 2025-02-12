package com.official.pium.exception;

public class AuthorizationException extends RuntimeException {

    public AuthorizationException() {
        super();
    }

    public AuthorizationException(final String message) {
        super(message);
    }

    public static class NeedAdminException extends AuthorizationException {

        public NeedAdminException() {
            super();
        }

        public NeedAdminException(final String message) {
            super(message);
        }
    }
}
