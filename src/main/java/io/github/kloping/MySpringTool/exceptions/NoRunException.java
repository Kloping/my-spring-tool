package io.github.kloping.MySpringTool.exceptions;


public class NoRunException extends RuntimeException {
    public NoRunException() {
        super("跳过不运行(jump don't run)");
    }

    public NoRunException(String s) {
        super(s);
    }

    private Object o;

    public NoRunException(Object s) {
        super();
        this.o = s;
    }
}