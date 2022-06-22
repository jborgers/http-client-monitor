package com.jpinpoint.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.pool.PoolStats;

import java.beans.ConstructorProperties;

@Slf4j
class PoolUsage extends PoolStats {

    @ConstructorProperties({"leased", "pending", "available", "max"})
    public PoolUsage(int leased, int pending, int available, int max) {
        super(leased, pending, available, max);
        log.debug("ctor " + this);
    }
}
