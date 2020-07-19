package org.jellyware.chassis.hermes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.activation.DataSource;
import javax.enterprise.inject.spi.CDI;
import javax.mail.internet.InternetAddress;

import lombok.Getter;
import lombok.Setter;

public interface Mailman extends Relay.For {
    Mailman to(InternetAddress address);

    Mailman cc(InternetAddress address);

    Mailman bcc(InternetAddress address);

    default Mailman self() {
        return Mailman.this;
    }

    public static Mailman of(Email email) {
        var tos = new HashSet<InternetAddress>();
        var ccs = new HashSet<InternetAddress>();
        var bccs = new HashSet<InternetAddress>();
        return new Mailman() {
            @Override
            public <T> Relay use(T disc) {
                return new Relay() {
                    @Override
                    public void send(String cfg, LocalDateTime at) {
                        CDI.current().getBeanManager().getEvent().select(new Conveyable.Literal())
                                .fire(new SmtpQueue<T>() {

                                    @Override
                                    public Email email() {
                                        return email;
                                    }

                                    @Override
                                    public Set<InternetAddress> tos() {
                                        return tos;
                                    }

                                    @Override
                                    public Set<InternetAddress> ccs() {
                                        return ccs;
                                    }

                                    @Override
                                    public Set<InternetAddress> bccs() {
                                        return bccs;
                                    }

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
                                });
                    }
                };
            }

            @Override
            public Mailman to(InternetAddress address) {
                tos.add(address);
                return self();
            }

            @Override
            public Mailman cc(InternetAddress address) {
                ccs.add(address);
                return self();
            }

            @Override
            public Mailman bcc(InternetAddress address) {
                bccs.add(address);
                return self();
            }
        };
    }

    public static interface Email {
        String sub();

        String msg();

        Set<DataSource> atts();

        public static class Builder {
            private String msg, sub;
            private Set<DataSource> atts;
            {
                atts = new HashSet<DataSource>();
            }

            public Builder(String subject) {
                super();
                this.sub = subject;
            }

            public Builder msg(String msg) {
                this.msg = msg;
                return this;
            }

            public Builder add(DataSource ds) {
                this.atts.add(ds);
                return this;
            }

            public Email build() {
                return new Email() {

                    @Override
                    public String sub() {
                        return sub;
                    }

                    @Override
                    public String msg() {
                        return msg;
                    }

                    @Override
                    public Set<DataSource> atts() {
                        return atts;
                    }
                };
            }
        }

        public static Builder of(String subject) {
            return new Builder(subject);
        }
    }

    public static interface SmtpQueue<T> {
        Email email();

        Set<InternetAddress> tos();

        Set<InternetAddress> ccs();

        Set<InternetAddress> bccs();

        T disc();

        LocalDateTime due();

        String cfg();
    }

    @Getter
    @Setter
    public static class Config {
        public static final String KEY = "mailman";
        private boolean ttls, auth;
        private int port;
        private String jndi, host, username, password;
    }
}
