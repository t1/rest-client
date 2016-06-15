package com.github.t1.rest;

import javax.jmdns.*;
import java.io.IOException;

public class JmDnsTestClient {
    public static class SampleListener implements ServiceListener {
        @Override
        public void serviceAdded(ServiceEvent event) {
            System.out.println("ADD: " + event.getInfo().getNiceTextString());
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            System.out.println("REMOVE: " + event.getName());
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            System.out.println("RESOLVED: " + event.getName());
        }
    }

    public static void main(String... args) throws IOException, InterruptedException {
        JmDNS jmdns = JmDNS.create();
        jmdns.addServiceListener("_http._tcp.local.", new SampleListener());
        Thread.sleep(10 * 60 * 1000);
    }
}
