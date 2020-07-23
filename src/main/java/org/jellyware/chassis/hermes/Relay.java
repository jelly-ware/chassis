package org.jellyware.chassis.hermes;

import java.time.LocalDateTime;

public interface Relay {
    void send(Object disc, LocalDateTime at);

    default void send(Object disc) {
        send(disc, LocalDateTime.now());
    };

    default void send(LocalDateTime at) {
        send(null, at);
    };

    default void send() {
        send((LocalDateTime) null);
    };
}
