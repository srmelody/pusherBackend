package com.rallydev.pusher;

/**
 * Created by smelody on 6/12/15.
 */
public class Project {
    private final String uuid;
    private final String oid;

    public Project(String oid, String uuid) {
        this.oid = oid;
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public String getOid() {
        return oid;
    }
}
