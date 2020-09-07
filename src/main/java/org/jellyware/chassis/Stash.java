package org.jellyware.chassis;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

public class Stash {
    public static final String PATH = "stash";
    public static final String ATT = "att";
    private final Rs.Service svc;

    @Inject
    public Stash(@Rs.Service.For Rs.Service svc) {
        super();
        Objects.requireNonNull(svc, "svc cannot be null");
        this.svc = svc;
    }

    public URI of(String id, boolean att) {
        return svc.of(Optional.of(List.of(PATH, name)), (att ? Optional.of(Stash.ATT) : Optional.empty()),
                Optional.empty());
    }

    public URI of(String id) {
        return of(name, false);
    }
}
