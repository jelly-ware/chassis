package org.jellyware.chassis.hermes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.core.Response;

import lombok.Getter;
import lombok.Setter;

public interface SMS extends Relay.For {
    SMS to(String recipient);

    SMS to(Set<String> recipient);

    default SMS self() {
        return SMS.this;
    }

    public static SMS of(String msg) {
        var rs = new HashSet<String>();
        return new SMS() {

            @Override
            public <T> Relay use(T disc) {
                return new Relay() {
                    @Override
                    public void send(String cfg, LocalDateTime at) {
                        rs.forEach(r -> {
                            CDI.current().getBeanManager().getEvent().select(new Conveyable.Literal())
                                    .fire(new Queue<T>() {

                                        @Override
                                        public T disc() {
                                            return disc;
                                        }

                                        @Override
                                        public LocalDateTime due() {
                                            return at;
                                        }

                                        @Override
                                        public String cfg() {
                                            return cfg;
                                        }

                                        @Override
                                        public String rcpt() {
                                            return r;
                                        }

                                        @Override
                                        public String msg() {
                                            return msg;
                                        }
                                    });
                        });
                    }
                };
            }

            @Override
            public SMS to(Set<String> recipient) {
                rs.addAll(recipient);
                return self();
            }

            @Override
            public SMS to(String recipient) {
                rs.add(msg);
                return self();
            }
        };
    }

    public static interface Queue<T> {
        T disc();

        LocalDateTime due();

        String cfg();

        String rcpt();

        String msg();
    }

    @Getter
    @Setter
    public static class Config {
        public static final String KEY = "sms";
        private String url, recipient, sender, msg, display;
        private int successResponseCode = Response.Status.OK.getStatusCode();
    }
}
