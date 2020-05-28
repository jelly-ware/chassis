package org.jellyware.chassis;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import javax.ws.rs.NameBinding;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;

import org.jellyware.beef.Beef;
import org.jellyware.trinity.Entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Jotter
 *
 */
public interface Exchange<T extends Entity<? extends Serializable>, U extends Entity<? extends Serializable>> {
    String BEARER = "Bearer";

    U login(Credentials credentials);

    void otp(Credentials credentials);

    void otp(T appuser);

    Optional<U> token(String token);

    U publicToken();

    void reset(T appuser, String password);

    void logout(U token);

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @NameBinding
    public static @interface Filter {
    }

    @javax.inject.Qualifier
    @Retention(RUNTIME)
    @Target({ METHOD, FIELD, PARAMETER })
    public @interface Qualifier {
    }

    @Getter
    @Setter
    public static class Credentials {
        private String email, phone, password, token;

        public boolean valid() {
            return (email == null) != (phone == null);
        }
    }

    public static void abort(ContainerRequestContext ctx, Beef beef, String realm) {
        ctx.abortWith(beef.response().header(HttpHeaders.WWW_AUTHENTICATE, Exchange.BEARER + " realm=\"" + realm + "\"")
                .build());
    }

    public static enum Authorization {
        NO_AUTH, BEARER_TOKEN;
    }

    public static boolean valid(Authorization type, String header) {
        var valid = false;
        switch (type) {
            case NO_AUTH:
                valid = header.isEmpty();
                break;
            case BEARER_TOKEN:
                valid = header.toLowerCase().startsWith(Exchange.BEARER.toLowerCase() + " ");
                break;

            default:
                break;
        }
        return valid;
    }

    public static String bearerToken(String header) {
        return header.substring(Exchange.BEARER.length()).trim();
    }
}
