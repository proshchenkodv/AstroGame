package ru.geekbrains.sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

import ru.geekbrains.base.Sprite;
import ru.geekbrains.exception.GameException;
import ru.geekbrains.math.Rect;
import ru.geekbrains.pool.BulletPool;

public class MainShip extends Sprite {

    private static final float SHIP_HEIGHT = 0.15f;
    private static final float BOTTOM_MARGIN = 0.05f;
    private static final int INVALID_POINTER = -1;

    private Rect worldBounds;
    private BulletPool bulletPool;
    private TextureRegion bulletRegion;
    private Vector2 bulletV;

    private float shootingTimer =0.0f;
    private float shootingInterval =0.4f;

    Sound bulletSound;
    Sound laserSound;
    Sound explosionSound;

    private final Vector2 v0;
    private final Vector2 v;

    private boolean pressedLeft;
    private boolean pressedRight;

    private int leftPointer = INVALID_POINTER;
    private int rightPointer = INVALID_POINTER;

    public MainShip(TextureAtlas atlas, BulletPool bulletPool) throws GameException {
        super(atlas.findRegion("main_ship"), 1, 2, 2);
        this.bulletPool = bulletPool;
        bulletRegion = atlas.findRegion("bulletMainShip");
        bulletSound = Gdx.audio.newSound(Gdx.files.internal("sounds/bullet.wav"));
        laserSound = Gdx.audio.newSound(Gdx.files.internal("sounds/laser.wav"));
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("sounds/explosion.wav"));
        bulletV = new Vector2(0, 0.5f);
        v0 = new Vector2(0.5f, 0);
        v = new Vector2();
    }

    @Override
    public void resize(Rect worldBounds) {
        this.worldBounds = worldBounds;
        setHeightProportion(SHIP_HEIGHT);
        setBottom(worldBounds.getBottom() + BOTTOM_MARGIN);
    }

    @Override
    public void update(float delta) {
        pos.mulAdd(v, delta);
        if (getLeft() < worldBounds.getLeft()) {
            setLeft(worldBounds.getLeft());
            stop();
        }
        if (getRight() > worldBounds.getRight()) {
            setRight(worldBounds.getRight());
            stop();
        }
        shootingTimer += delta;
        if (shootingTimer >= shootingInterval) {
            shootingTimer = 0;
            this.shoot();
        }
    }

    @Override
    public boolean touchDown(Vector2 touch, int pointer, int button) {
        if (touch.x < worldBounds.pos.x) {
            if (leftPointer != INVALID_POINTER) {
                return false;
            }
            leftPointer = pointer;
            moveLeft();
        } else {
            if (rightPointer != INVALID_POINTER) {
                return false;
            }
            rightPointer = pointer;
            moveRight();
        }
        return false;
    }

    @Override
    public boolean touchUp(Vector2 touch, int pointer, int button) {
        if (pointer == leftPointer) {
            leftPointer = INVALID_POINTER;
            if (rightPointer != INVALID_POINTER) {
                moveRight();
            } else {
                stop();
            }
        } else if (pointer == rightPointer) {
            rightPointer = INVALID_POINTER;
            if (leftPointer != INVALID_POINTER) {
                moveLeft();
            } else {
                stop();
            }
        }
        return false;
    }

    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.LEFT:
                pressedLeft = true;
                moveLeft();
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                pressedRight = true;
                moveRight();
                break;
            case Input.Keys.UP:
                shoot();
        }
        return false;
    }

    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A:
            case Input.Keys.LEFT:
                pressedLeft = false;
                if (pressedRight) {
                    moveRight();
                } else {
                    stop();
                }
                break;
            case Input.Keys.D:
            case Input.Keys.RIGHT:
                pressedRight = false;
                if (pressedLeft) {
                    moveLeft();
                } else {
                    stop();
                }
                break;
        }
        return false;
    }

    public void shoot() {
        Bullet bullet = bulletPool.obtain();
        bullet.set(this, bulletRegion, pos, bulletV, 0.01f, worldBounds, 1);
        bulletSound.play(0.005f);

    }

    private void moveRight() {
        v.set(v0);
    }

    private void moveLeft() {
        v.set(v0).rotate(180);
    }

    private void stop() {
        v.setZero();
    }

    @Override
    public void dispose() {
        bulletPool.dispose();
        bulletSound.dispose();
        laserSound.dispose();
        explosionSound.dispose();
        super.dispose();
    }
}