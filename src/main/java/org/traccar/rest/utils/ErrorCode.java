package org.traccar.rest.utils;

/**
 * Created by niko on 2/28/16.
 */
public enum ErrorCode {
    USER_EXISTS("userExists"),
    COMPANY_EXISTS("companyExists");

    private String code;

    private ErrorCode(String code) {
        this.code = code;
    }

    public String value() {
        return code;
    }

}
