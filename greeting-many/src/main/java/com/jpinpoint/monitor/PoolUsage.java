package com.jpinpoint.monitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.pool.PoolStats;

import java.beans.ConstructorProperties;

@Slf4j
class PoolUsage extends PoolStats {
    //private final String targetHost;

    @ConstructorProperties({/*"targetHost",*/ "leased", "pending", "available", "max"})
    public PoolUsage(/*String targetHost,*/ int leased, int pending, int available, int max) {
        super(leased, pending, available, max);
        //this.targetHost = targetHost;
        log.debug("ctor " + this);
    }

//    public String getTargetHost() {
//        return targetHost;
//    }

 //   @Override
//    public String toString() {
//        return "PoolUsage targetHost='" + targetHost + "': " + super.toString();
//    }
}
