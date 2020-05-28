package org.jellyware.chassis.hermes;

import java.time.LocalDateTime;

import org.jellyware.chassis.Constant;

public interface Relay {
    void send(String cfg, LocalDateTime at);

    default void send(String cfg) {
        send(cfg, LocalDateTime.now());
    };

    default void send(LocalDateTime at) {
        send(Constant.DEFAULT, at);
    };

    default void send() {
        send(Constant.DEFAULT);
    };

    @FunctionalInterface
    public static interface For {
        <T> Relay use(T disc);
    }
}
