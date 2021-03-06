package ch.logixisland.anuto.game.entity.tower;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

import ch.logixisland.anuto.game.entity.enemy.Enemy;
import ch.logixisland.anuto.game.entity.Entity;
import ch.logixisland.anuto.game.entity.plateau.Plateau;
import ch.logixisland.anuto.game.render.Drawable;
import ch.logixisland.anuto.game.render.Layers;
import ch.logixisland.anuto.game.TickTimer;
import ch.logixisland.anuto.game.entity.Types;
import ch.logixisland.anuto.game.data.Path;
import ch.logixisland.anuto.game.data.TowerConfig;
import ch.logixisland.anuto.util.iterator.StreamIterator;
import ch.logixisland.anuto.util.math.vector.Intersections;
import ch.logixisland.anuto.util.math.MathUtils;
import ch.logixisland.anuto.util.math.vector.Vector2;

public abstract class Tower extends Entity {

    /*
    ------ Constants ------
     */

    public static final int TYPE_ID = Types.TOWER;

    /*
    ------ RangeIndicator Class ------
     */

    private class RangeIndicator implements Drawable {
        private Paint mPen;

        public RangeIndicator() {
            mPen = new Paint();
            mPen.setStyle(Paint.Style.STROKE);
            mPen.setStrokeWidth(0.05f);
            mPen.setColor(Color.GREEN);
            mPen.setAlpha(128);
        }

        @Override
        public int getLayer() {
            return Layers.TOWER_RANGE;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawCircle(getPosition().x, getPosition().y, getRange(), mPen);
        }
    }

    /*
    ------ PathSection Class ------
     */

    public class PathSection {
        public Vector2 p1;
        public Vector2 p2;
        public float len;
    }

    /*
    ------ Members ------
     */

    private TowerConfig mConfig;

    private int mValue;
    private int mLevel;
    private float mDamage;
    private float mRange;
    private float mReloadTime;
    private float mDamageInflicted;

    private Plateau mPlateau = null;
    private boolean mReloaded = false;

    private TickTimer mReloadTimer;
    private RangeIndicator mRangeIndicator;

    /*
    ------ Constructors ------
     */

    public Tower() {
        mConfig = getGameManager().getLevel().getTowerConfig(this);

        mValue = mConfig.getValue();
        mDamage = mConfig.getDamage();
        mRange = mConfig.getRange();
        mReloadTime = mConfig.getReload();
        mLevel = 1;

        mReloadTimer = TickTimer.createInterval(mReloadTime);

        setEnabled(false);
    }

    /*
    ------ Methods ------
     */

    @Override
    public final int getType() {
        return TYPE_ID;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void clean() {
        super.clean();
        hideRange();
        setPlateau(null);
    }

    @Override
    public void tick() {
        super.tick();

        if (isEnabled() && !mReloaded && mReloadTimer.tick()) {
            mReloaded = true;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (enabled) {
            mReloaded = true;
        }
    }

    public abstract void preview(Canvas canvas);


    public Plateau getPlateau() {
        return mPlateau;
    }

    public void setPlateau(Plateau plateau) {
        if (mPlateau != null) {
            mPlateau.setOccupant(null);
        }

        mPlateau = plateau;

        if (mPlateau != null) {
            mPlateau.setOccupant(this);
            setPosition(mPlateau.getPosition());
        }
    }


    public boolean isReloaded() {
        return mReloaded;
    }

    public void setReloaded(boolean reloaded) {
        mReloaded = reloaded;
    }


    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public float getDamage() {
        return mDamage;
    }

    public float getRange() {
        return mRange;
    }

    public float getReloadTime() {
        return mReloadTime;
    }


    public float getDamageInflicted() {
        return mDamageInflicted;
    }

    public void reportDamageInflicted(float damage) {
        mDamageInflicted += damage;
    }


    public void buy() {
        getGameManager().takeCredits(mValue);
        mValue *= getGameManager().getSettings().getAgeModifier();
    }

    public void sell() {
        getGameManager().giveCredits(mValue, false);
    }

    public void devalue(float factor) {
        mValue *= factor;
    }

    public Tower upgrade() {
        Plateau plateau = this.getPlateau();
        Tower upgrade;

        try {
            upgrade = mConfig.getUpgradeTowerConfig().getTowerClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        int cost = getUpgradeCost();
        getGameManager().takeCredits(cost);
        upgrade.mValue = this.mValue + cost;

        this.remove();
        upgrade.setPlateau(plateau);
        upgrade.setEnabled(true);
        getGameEngine().add(upgrade);

        return upgrade;
    }

    public boolean isUpgradeable() {
        return mConfig.getUpgradeTowerConfig() != null;
    }

    public int getUpgradeCost() {
        if (!isUpgradeable()) {
            return -1;
        }

        return mConfig.getUpgradeTowerConfig().getValue() - mConfig.getValue();
    }

    public void enhance() {
        getGameManager().takeCredits(getEnhanceCost());

        mValue += getEnhanceCost();
        mDamage += mConfig.getEnhanceDamage() * (float)Math.pow(mConfig.getEnhanceBase(), mLevel - 1);
        mRange += mConfig.getEnhanceRange();
        mReloadTime -= mConfig.getEnhanceReload();

        mLevel++;

        mReloadTimer.setInterval(mReloadTime);
    }

    public boolean isEnhanceable() {
        return mLevel < mConfig.getMaxLevel();
    }

    public int getEnhanceCost() {
        if (!isEnhanceable()) {
            return -1;
        }

        return Math.round(mConfig.getEnhanceCost() * (float)Math.pow(mConfig.getEnhanceBase(), mLevel - 1));
    }

    public int getLevel() {
        return mLevel;
    }

    public int getLevelMax() {
        return mConfig.getMaxLevel();
    }


    public void showRange() {
        if (mRangeIndicator == null) {
            mRangeIndicator = new RangeIndicator();
            getGameEngine().add(mRangeIndicator);
        }
    }

    public void hideRange() {
        if (mRangeIndicator != null) {
            getGameEngine().remove(mRangeIndicator);
            mRangeIndicator = null;
        }
    }


    public StreamIterator<Enemy> getPossibleTargets() {
        return getGameEngine().get(Enemy.TYPE_ID)
                .filter(inRange(getPosition(), getRange()))
                .cast(Enemy.class);
    }

    public List<PathSection> getPathSections() {
        List<PathSection> ret = new ArrayList<>();

        float r2 = MathUtils.square(getRange());

        for (Path p : getGameManager().getLevel().getPaths()) {
            for (int i = 1; i < p.size(); i++) {
                Vector2 p1 = p.get(i - 1).copy().sub(getPosition());
                Vector2 p2 = p.get(i).copy().sub(getPosition());

                boolean p1in = p1.len2() <= r2;
                boolean p2in = p2.len2() <= r2;

                Vector2[] is = Intersections.lineCircle(p1, p2, getRange());

                PathSection s = new PathSection();

                if (p1in && p2in) {
                    s.p1 = p1.add(getPosition());
                    s.p2 = p2.add(getPosition());
                } else if (!p1in && !p2in) {
                    if (is == null) {
                        continue;
                    }

                    float a1 = Vector2.fromTo(is[0], p1).angle();
                    float a2 = Vector2.fromTo(is[0], p2).angle();

                    if (MathUtils.equals(a1, a2, 10f)) {
                        continue;
                    }

                    s.p1 = is[0].add(getPosition());
                    s.p2 = is[1].add(getPosition());
                }
                else {
                    float angle = Vector2.fromTo(p1, p2).angle();

                    if (p1in) {
                        if (MathUtils.equals(angle, Vector2.fromTo(p1, is[0]).angle(), 10f)) {
                            s.p2 = is[0].add(getPosition());
                        } else {
                            s.p2 = is[1].add(getPosition());
                        }

                        s.p1 = p1.add(getPosition());
                    } else {
                        if (MathUtils.equals(angle, Vector2.fromTo(is[0], p2).angle(), 10f)) {
                            s.p1 = is[0].add(getPosition());
                        } else {
                            s.p1 = is[1].add(getPosition());
                        }

                        s.p2 = p2.add(getPosition());
                    }
                }

                s.len = Vector2.fromTo(s.p1, s.p2).len();
                ret.add(s);
            }
        }

        return ret;
    }


    public TowerConfig getConfig() {
        return mConfig;
    }

    public float getProperty(String name) {
        return mConfig.getProperties().get(name);
    }
}
