package org.jellyware.chassis.hermes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;

public interface SMS extends Relay {
    SMS to(String recipient);

    SMS to(Set<String> recipient);

    default SMS self() {
        return SMS.this;
    }

    public static SMS of(String msg) {
        var rs = new HashSet<String>();
        return new SMS() {

            @Override
            public void send(Object disc, LocalDateTime at) {
                CDI.current().getBeanManager().getEvent().select(new Conveyable.Literal()).fire(new Queue() {

                    @Override
                    public Object disc() {
                        return disc;
                    }

                    @Override
                    public LocalDateTime due() {
                        return at;
                    }

                    @Override
                    public Set<String> recipients() {
                        return rs;
                    }

                    @Override
                    public String message() {
                        return msg;
                    }
                });
            }

            @Override
            public SMS to(Set<String> recipients) {
                rs.addAll(recipients);
                return self();
            }

            @Override
            public SMS to(String recipient) {
                rs.add(recipient);
                return self();
            }
        };
    }

    public static interface Queue {
        Object disc();

        LocalDateTime due();

        Set<String> recipients();

        String message();
    }
}
