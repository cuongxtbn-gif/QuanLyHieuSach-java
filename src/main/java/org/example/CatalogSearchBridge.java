package org.example;

/**
 * Pass search text from homepage / cart into the catalog screen on next load.
 */
public final class CatalogSearchBridge {

    private static String pendingQuery = "";

    private CatalogSearchBridge() {}

    public static void setPendingQuery(String query) {
        pendingQuery = query != null ? query.trim() : "";
    }

    public static String consumePendingQuery() {
        String q = pendingQuery;
        pendingQuery = "";
        return q;
    }
}
