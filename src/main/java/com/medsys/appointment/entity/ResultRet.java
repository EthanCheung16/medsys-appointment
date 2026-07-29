package com.medsys.appointment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Uniform response envelope returned by every endpoint of the appointment module.
 * Carries a result code, a human-readable message and the payload, so that client
 * applications handle success and business-rule rejections consistently.
 *
 * @author ZHANGYIXUAN
 * @version 1.0.0
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResultRet {

    /**
     * 0：error
     * 1：success
     */
    private Integer code;

    /**
     * return message
     */
    private String message;

    /**
     * return data
     */
    private Object resultData;


    /**
     * return success
     */
    public static ResultRet okResult(){
        return new ResultRet(1, "success",null);
    }
    public static ResultRet okResult(Object data) {
        return new ResultRet(1, "success", data);
    }
    public static ResultRet okResult(String message, Object data) {
        return new ResultRet(1, message, data);
    }

    /**
     * return error
     */
    public static ResultRet errorResult() {
        return new ResultRet(0, "error", null);
    }
    public static ResultRet errorResult(String message) {
        return new ResultRet(0, message, null);
    }

}
