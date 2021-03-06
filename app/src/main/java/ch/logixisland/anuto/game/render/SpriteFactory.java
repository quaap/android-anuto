package ch.logixisland.anuto.game.render;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import ch.logixisland.anuto.game.theme.ThemeManager;

public class SpriteFactory {

    private final Resources mResources;
    private final ThemeManager mThemeManager;

    public SpriteFactory(Resources resources, ThemeManager themeManager) {
        mResources = resources;
        mThemeManager = themeManager;
    }

    public SpriteTemplate createTemplate(int resourceId, int spriteCount) {
        resourceId = mThemeManager.getTheme().resourceMap(resourceId);

        Bitmap sheet = BitmapFactory.decodeResource(mResources, resourceId);
        Bitmap[] sprites = new Bitmap[spriteCount];
        int spriteWidth = sheet.getWidth() / spriteCount;
        int spriteHeight = sheet.getHeight();

        for (int i = 0; i < spriteCount; i++) {
            sprites[i] = Bitmap.createBitmap(sheet, spriteWidth * i, 0, spriteWidth, spriteHeight);
        }

        return new SpriteTemplate(sprites);
    }

    public StaticSprite createStatic(int layer, SpriteTemplate template) {
        return new StaticSprite(layer, template);
    }

    public AnimatedSprite createAnimated(int layer, SpriteTemplate template) {
        return new AnimatedSprite(layer, template);
    }

    public ReplicatedSprite createReplication(SpriteInstance original) {
        return new ReplicatedSprite(original);
    }

}
