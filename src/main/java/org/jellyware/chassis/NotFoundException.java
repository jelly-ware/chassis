package org.jellyware.chassis;

import javax.ws.rs.core.Response;

import org.jellyware.beef.Beef.UncheckedException;

public class NotFoundException extends UncheckedException {
    private static final long serialVersionUID = 7918826341720024234L;

    @Override
    public Response.Status httpStatus() {
        return Response.Status.NOT_FOUND;
    }

    public NotFoundException(org.jellyware.beef.Error.Builder error, Throwable cause) {
        super(error, cause);
    }

    public NotFoundException(org.jellyware.beef.Error.Builder error) {
        super(error);
    }

    public NotFoundException(Throwable cause) {
        super(cause);
    }

    public NotFoundException() {
    }
}