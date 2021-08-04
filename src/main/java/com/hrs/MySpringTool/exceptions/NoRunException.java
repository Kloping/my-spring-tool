package com.hrs.MySpringTool.exceptions;


public class NoRunException extends RuntimeException{
    public NoRunException() {
        super("跳过不运行");
    }

    public NoRunException(String s) {
        super(s);
    }
}