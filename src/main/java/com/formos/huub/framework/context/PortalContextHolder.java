package com.formos.huub.framework.context;

import java.util.Objects;
import java.util.UUID;

/**
 * ***************************************************
 * * Description :
 * * File        : PortalContextHolder
 * * Author      : Hung Tran
 * * Date        : Sep 28, 2024
 * ***************************************************
 **/
public class PortalContextHolder {

    private static final ThreadLocal<PortalContext> contextHolder = new ThreadLocal<>();

    public static void setContext(PortalContext context) {
        contextHolder.set(context);
    }

    public static PortalContext getContext() {
        return contextHolder.get();
    }

    public static void clearContext() {
        contextHolder.remove();
    }

    public static UUID getPortalId() {
        var portalContext = contextHolder.get();
        return Objects.nonNull(portalContext) ? portalContext.getPortalId() : null;
    }

    public static String getOriginUrl() {
        var portalContext = contextHolder.get();
        return Objects.nonNull(portalContext) ? portalContext.getUrl(): null;
    }

}
