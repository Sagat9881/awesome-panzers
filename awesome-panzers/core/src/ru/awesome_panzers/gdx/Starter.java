package ru.awesome_panzers.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Starter extends ApplicationAdapter {

    private final KeyBoardAdapter inputProcessor;
    private List<Panzer> enemyList = new ArrayList<>();
    private SpriteBatch batch;
    private Panzer me;
    private MessageSender sender;
    public Starter(InputState inputState) {
        this.inputProcessor = new KeyBoardAdapter(inputState);
    }

    @Override
    public void create() {

        Gdx.input.setInputProcessor(inputProcessor);
        List<Panzer> newEnemy = IntStream.range(0, 15).mapToObj(i -> {
            int x = MathUtils.random(Gdx.graphics.getWidth());
            int y = MathUtils.random(Gdx.graphics.getHeight());

            return new Panzer(x, y, "panzer_bad.jpg");
        }).collect(Collectors.toList());
        enemyList.addAll(newEnemy);
        batch = new SpriteBatch();
        me = new Panzer(100, 200);
    }

    @Override
    public void render() {
        me.moveTo(inputProcessor.getDirection());
        me.rotateTo(inputProcessor.getMousePosition());
        ScreenUtils.clear(1, 0, 0, 1);
        batch.begin();
        me.render(batch);
        enemyList.forEach(enemy -> {
            enemy.rotateTo(me.getPosition());
            enemy.render(batch);
        });
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        me.dispose();
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public void handleTimer() {
        if(Objects.nonNull(inputProcessor)){
            InputState playerState = inputProcessor.updateAndGetInputState(me.getOrigin());
            sender.sendMessage(playerState);
        }
    }
}
