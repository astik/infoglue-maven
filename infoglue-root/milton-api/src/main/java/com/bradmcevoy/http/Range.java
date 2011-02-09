package com.bradmcevoy.http;
 


public class Range {
    private final long start;
    private final long finish;
    
    public Range(long start, long finish) {
        this.start = start;
        this.finish = finish;
    }

    public long getStart() {
        return start;
    }

    public long getFinish() {
        return finish;
    }

    @Override
    public String toString() {
        return "bytes " + start + "-" + finish;
    }


}
