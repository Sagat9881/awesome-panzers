package ru.awesome_panzers.gdx.emitter;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Particle implements Pool.Poolable {
    private String owner;
    private float size;
    private float speed;
    private float distance2;

    private final Vector2 position = new Vector2();
    private final Vector2 startPoint = new Vector2();
    private final Vector2 nextStepPoint = new Vector2(1, 1);


    public void act(float delta) {
        float stepLength = speed * delta;
        nextStepPoint.setLength(stepLength);
        position.add(nextStepPoint);

    }

    public void fill(String owner, Vector2 position, float angle, float size, float speed, float distance) {
        this.owner = owner;
        this.position.set(position);
        this.startPoint.set(position);
        this.nextStepPoint.setAngleDeg(angle);
        this.size = size;
        this.speed = speed;
        this.distance2 = distance * distance;
    }

    @Override
    public void reset() {
        this.owner = "";
        this.position.set(0, 0);
        this.startPoint.set(0, 0);
        this.nextStepPoint.set(1, 1);
        this.size = 0;
        this.speed = 0;
        this.distance2 = 0;
    }

    public Vector2 getPosition() {
        return position;
    }

    public boolean isFinished() {
        return position.dst2(startPoint) >=distance2;
    }
}
