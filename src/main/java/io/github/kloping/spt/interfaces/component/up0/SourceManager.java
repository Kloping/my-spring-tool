package io.github.kloping.spt.interfaces.component.up0;

/**
 * source manager
 *
 * @author github.kloping
 */
public interface SourceManager extends BaseManager {
    /**
     * no detail intro
     *
     * @param name
     * @param <T>
     * @return
     */
    <T> T get(String name);
}
