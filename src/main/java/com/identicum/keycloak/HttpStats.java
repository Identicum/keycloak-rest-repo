package com.identicum.keycloak;

import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;

import java.util.TimerTask;

@AllArgsConstructor
public class HttpStats extends TimerTask {

    public static final Integer TO_MILLISECONDS = 1000;

    private Logger logger;
    private RestHandler restHandler;

    @Override
    public void run() {
        logger.infov("HTTP pool stats: {0}", restHandler.getStats().toString());
    }
}
