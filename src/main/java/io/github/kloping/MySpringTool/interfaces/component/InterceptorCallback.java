package io.github.kloping.MySpringTool.interfaces.component;


public interface InterceptorCallback {
    /**
     * 添加拦截器
     *
     * @param c        拦截几次
     * @param callback 拦截回调
     * @param filter   拦截条件
     * @return
     */
    int addIntercept(int c, io.github.kloping.MySpringTool.interfaces.component.Callback callback, io.github.kloping.MySpringTool.interfaces.component.Filter filter);

    /**
     * 添加拦截器
     *
     * @param c      拦截几次
     * @param filter 拦截条件
     * @return
     */
    int addIntercept(int c, io.github.kloping.MySpringTool.interfaces.component.Filter filter);
}
