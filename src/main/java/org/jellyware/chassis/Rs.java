package org.jellyware.chassis;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.jellyware.beef.Beef;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Jotter
 *
 */
public class Rs {
    public static final String PATH = "rs";

    public final static String[] svc(UriInfo ui) {
        return svc(ui.getPathSegments());
    }

    public final static String[] svc(List<PathSegment> segments) {
        return Arrays.copyOf(segments.stream().map(PathSegment::getPath).toArray(String[]::new), segments.size() - 1);
    }

    public final static String op(UriInfo ui) {
        return op(ui.getPathSegments());
    }

    public final static String op(List<PathSegment> segments) {
        return segments.get(segments.size() - 1).getPath();
    }

    public static class Service {
        private Config cfg;

        public Service(Config cfg) {
            super();
            Objects.requireNonNull(cfg, "cfg cannot be null");
            this.cfg = cfg;
        }

        public URI of(Optional<List<String>> path, Optional<String> op, Optional<Map<String, String>> query) {
            try {
                return new URI(cfg.getScheme(), null, cfg.getServerName(), cfg.getPort(),
                        org.jellyware.toolkit.URI.path(
                                Stream.concat(Stream.concat(Stream.of(cfg.getContextPath(), Rs.PATH),
                                        path.orElse(List.of()).stream()), op.stream()).filter(str -> str != null)
                                        .collect(Collectors.toList()),
                                org.jellyware.toolkit.URI.encoder(StandardCharsets.UTF_8)),
                        org.jellyware.toolkit.URI.query(query.orElse(Map.of()),
                                org.jellyware.toolkit.URI.encoder(StandardCharsets.UTF_8)),
                        null);
            } catch (Exception e) {
                throw Beef.uncheck(e);
            }
        }

        @Getter
        @Setter
        public static class Config {
            public static final String KEY = "rs-side-cfg";
            private String scheme, serverName, contextPath;
            private int port;
        }

        @Retention(RUNTIME)
        @Target({ TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR })
        @Qualifier
        public static @interface For {
            Side value() default Side.SVR;

            @SuppressWarnings("all")
            public static class Literal extends AnnotationLiteral<For> implements For {
                private final Side side;

                public Literal(Side side) {
                    super();
                    this.side = side;
                }

                @Override
                public Side value() {
                    return side;
                }

            }

            public static enum Side {
                LCL, SVR, RMT;
            }
        }
    }
}
