package org.jellyware.chassis;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.jellyware.chassis.rpt.Report;
import org.jellyware.chassis.rpt.Template;

/**
 * @author Jotter
 *
 */
public class Rs {
    public static final String PATH = "rs";

    public static final String BASE_URI = "BASE_URI";

    public static final String STASH = "stash";
    public static final String ATT = "att";

    private final String base;

    public Rs() {
        this(System.getenv(Rs.BASE_URI));
    }

    public Rs(String base) {
        var sb = new StringBuilder(base);
        if (sb.charAt(sb.length() - 1) != '/')
            sb.append('/');
        sb.append(Rs.PATH);
        this.base = sb.toString();
    }

    // rs

    public URI compose(String op, List<String> svc) {
        var path = new ArrayList<>(svc);
        path.add(op);
        return URI.create(base + org.jellyware.toolkit.URI.path(path));
    }

    public URI compose(String op, String... svc) {
        var path = new ArrayList<>(List.of(svc));
        path.add(op);
        return URI.create(base + org.jellyware.toolkit.URI.path(path));
    }

    // uri

    public URI uri(List<String> path, Map<String, String> query) {
        return URI.create(base + org.jellyware.toolkit.URI.path(path) + '?' + org.jellyware.toolkit.URI.query(query));
    }

    public URI uri(List<String> path) {
        return uri(path, Map.of());
    }

    public URI uri(String... path) {
        return uri(List.of(path));
    }

    // stash

    public URI stash(String id, boolean att) {
        var path = new ArrayList<>(List.of(Rs.STASH, id));
        if (att)
            path.add(Rs.ATT);
        return uri(path);
    }

    public URI stash(String id) {
        return stash(id, false);
    }

    // rpt

    public URI report(String name, Template.Streamer.Format format, Map<String, String> params, boolean att) {
        var path = new ArrayList<>(List.of(Report.PATH, name, format.getExtension()));
        if (att)
            path.add(Rs.ATT);
        return uri(path, params);
    };

    public URI report(String name, Template.Streamer.Format format, Map<String, String> params) {
        return report(name, format, params, false);
    }

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
}
