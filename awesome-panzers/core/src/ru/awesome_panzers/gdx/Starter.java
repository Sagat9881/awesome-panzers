package ru.awesome_panzers.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import ru.awesome_panzers.gdx.emitter.Emitter;
import ru.awesome_panzers.gdx.emitter.Particle;

import java.util.Objects;

public class Starter extends ApplicationAdapter {

    private final KeyBoardAdapter inputProcessor;
    private SpriteBatch batch;

    private String meId;
    private ObjectMap<String, Panzer> panzers = new ObjectMap<>();
    private MessageSender sender;
    private Texture bulletTexture;

    public Starter(InputState inputState) {
        this.inputProcessor = new KeyBoardAdapter(inputState);
    }

    @Override
    public void create() {

        Gdx.input.setInputProcessor(inputProcessor);
        batch = new SpriteBatch();

        Panzer me = new Panzer(100, 200);
        panzers.put(meId, me);

        bulletTexture = new Texture("panzer_bullet.png");
    }

    @Override
    public void render() {

        ScreenUtils.clear(1, 0, 0, 1);
        batch.begin();
        for (String key : panzers.keys()) {
            Panzer panzer = panzers.get(key);
            InputState inputState = inputProcessor.getInputState();

            Emitter emitter = panzer.emitter;
            emitter.setAngle(inputState.getAngle());
            emitter.getPosition().set(panzer.getOrigin());
            float deltaTime = Gdx.graphics.getDeltaTime();
            if (inputState.isFirePressed()) {
                emitter.start(deltaTime);
            }

            emitter.act(deltaTime);
            for (Particle particle : emitter.getParticles()) {
                Vector2 position = particle.getPosition();
                batch.draw(bulletTexture, position.x, position.y);
            }
            panzer.render(batch);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        for (Panzer value : panzers.values()) {
            value.dispose();
        }
    }

    public void setSender(MessageSender sender) {
        this.sender = sender;
    }

    public void handleTimer() {
        if (Objects.nonNull(inputProcessor) && !panzers.isEmpty()) {
            Panzer me = panzers.get(meId);
            InputState playerState = inputProcessor.updateAndGetInputState(me.getOrigin());
            sender.sendMessage(playerState);
        }
    }

    public void setMeId(String meId) {
        this.meId = meId;
    }

    public void evict(String idToEvict) {
        panzers.remove(idToEvict);
    }

    public void updatePanzer(String id, float x, float y, float angle) {
        if (panzers.isEmpty()) {
            return;
        }

        Panzer panzer = panzers.get(id);
        if (panzer == null) {
            panzer = new Panzer(x, y, "panzer_bad.jpg");
            panzers.put(id, panzer);
        } else {
            panzer.moveTo(x, y);
        }
        panzer.rotateTo(angle);
    }
}
