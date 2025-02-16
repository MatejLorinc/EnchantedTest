package com.matejlorinc.enchanted.entity;

import java.time.Duration;
import java.time.Instant;

public class PigLockout {
    private final CustomPig entity;
    private final Instant started;
    private final Duration duration;
    private final String animation;

    public PigLockout(CustomPig entity, Instant started, Duration duration, String animation) {
        this.entity = entity;
        this.started = started;
        this.duration = duration;
        this.animation = animation;

        //TODO Play custom animation from resource pack on entity
    }

    public boolean isLocked() {
        return started.plus(duration).isAfter(Instant.now());
    }
}
