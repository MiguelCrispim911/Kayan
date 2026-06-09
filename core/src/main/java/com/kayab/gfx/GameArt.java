package com.kayab.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.kayab.entities.Boss;
import com.kayab.entities.Enemy;

/**
 * US-12 + mejora visual — Pixel-art por código (Pixmap -> Texture), sin PNGs.
 * Ahora con CONTORNO y SOMBREADO (luz arriba-izq, sombra abajo-der) en cada
 * bloque, y TILES de terreno (pasto + tierra / piedra) por mundo.
 * La física y colisiones (Box2D) NO cambian: esto solo dibuja encima.
 */
public class GameArt {

    // Paleta
    private static final Color SKIN    = new Color(0.85f, 0.66f, 0.45f, 1f);
    private static final Color HAIR    = new Color(0.12f, 0.08f, 0.05f, 1f);
    private static final Color BELT     = new Color(0.80f, 0.68f, 0.30f, 1f);
    private static final Color BOW      = new Color(0.55f, 0.35f, 0.15f, 1f);
    private static final Color KAYAB    = new Color(0.20f, 0.35f, 0.85f, 1f);
    private static final Color FEATHR   = new Color(0.90f, 0.20f, 0.20f, 1f);
    private static final Color OUTLINE  = new Color(0.07f, 0.05f, 0.04f, 1f);
    private static final Color GOLD     = new Color(1f, 0.84f, 0f, 1f);

    private static final Color E_BASIC  = new Color(0.85f, 0.12f, 0.12f, 1f);
    private static final Color E_SHIELD = new Color(0.95f, 0.55f, 0.10f, 1f);
    private static final Color E_SWORD  = new Color(0.55f, 0.55f, 0.58f, 1f);
    private static final Color E_GUN    = new Color(0.90f, 0.90f, 0.92f, 1f);

    private final Array<Texture> textures = new Array<>();

    private final Animation<TextureRegion> playerIdle;
    private final Animation<TextureRegion> playerRun;
    private final Animation<TextureRegion> playerJump;
    private final Animation<TextureRegion> playerShoot;

    private final Animation<TextureRegion>[] enemyWalk; // por Enemy.Type.ordinal()

    private final Animation<TextureRegion> bossXocotl;
    private final Animation<TextureRegion> bossVargas;

    private final TextureRegion arrowPlayer;
    private final TextureRegion arrowEnemy;
    private final TextureRegion[] tiles; // por mundo 1..4
    private final TextureRegion white;

    @SuppressWarnings("unchecked")
    public GameArt() {
        playerIdle  = anim(0.35f, humanoid(KAYAB, FEATHR, false, 0, false, false),
                                  humanoid(KAYAB, FEATHR, false, 4, false, false));
        playerRun   = anim(0.12f, humanoid(KAYAB, FEATHR, false, 1, false, false),
                                  humanoid(KAYAB, FEATHR, false, 2, false, false),
                                  humanoid(KAYAB, FEATHR, false, 1, false, false),
                                  humanoid(KAYAB, FEATHR, false, 3, false, false));
        playerJump  = anim(1f,    humanoid(KAYAB, FEATHR, false, 0, false, true));
        playerShoot = anim(0.09f, humanoid(KAYAB, FEATHR, false, 0, true, false),
                                  humanoid(KAYAB, FEATHR, false, 4, true, false));

        enemyWalk = new Animation[Enemy.Type.values().length];
        enemyWalk[Enemy.Type.BASIC.ordinal()]  = enemyAnim(E_BASIC,  false);
        enemyWalk[Enemy.Type.SHIELD.ordinal()] = enemyAnim(E_SHIELD, true);
        enemyWalk[Enemy.Type.SWORD.ordinal()]  = enemyAnim(E_SWORD,  false);
        enemyWalk[Enemy.Type.GUN.ordinal()]    = enemyAnim(E_GUN,    false);

        bossXocotl = anim(0.45f, boss(28, 40, new Color(0.60f, 0f, 0f, 1f), 0),
                                 boss(28, 40, new Color(0.60f, 0f, 0f, 1f), 1));
        bossVargas = anim(0.45f, boss(20, 38, new Color(0.30f, 0.32f, 0.36f, 1f), 0),
                                 boss(20, 38, new Color(0.30f, 0.32f, 0.36f, 1f), 1));

        arrowPlayer = arrow(new Color(1f, 0.92f, 0.2f, 1f));
        arrowEnemy  = arrow(new Color(1f, 0.55f, 0.1f, 1f));

        tiles = new TextureRegion[5];
        tiles[1] = grassTile(new Color(0.30f, 0.62f, 0.26f, 1f), new Color(0.46f, 0.33f, 0.19f, 1f)); // floresta
        tiles[2] = grassTile(new Color(0.34f, 0.52f, 0.34f, 1f), new Color(0.40f, 0.39f, 0.34f, 1f)); // selva gris
        tiles[3] = stoneTile(new Color(0.46f, 0.30f, 0.17f, 1f));                                     // piedra ardiente
        tiles[4] = stoneTile(new Color(0.33f, 0.34f, 0.37f, 1f));                                     // piedra gris fría

        white = buildWhite();
    }

    // ---------- Bloque sombreado con contorno ----------

    private void block(Pixmap pm, int x, int y, int w, int h, Color base) {
        pm.setColor(OUTLINE);     pm.fillRectangle(x - 1, y - 1, w + 2, h + 2);
        pm.setColor(base);        pm.fillRectangle(x, y, w, h);
        pm.setColor(light(base)); pm.fillRectangle(x, y, w, 1);         pm.fillRectangle(x, y, 1, h);
        pm.setColor(dark(base));  pm.fillRectangle(x, y + h - 1, w, 1); pm.fillRectangle(x + w - 1, y, 1, h);
    }

    private Color light(Color c) { return new Color(Math.min(1f, c.r + 0.18f), Math.min(1f, c.g + 0.18f), Math.min(1f, c.b + 0.18f), 1f); }
    private Color dark(Color c)  { return new Color(c.r * 0.58f, c.g * 0.58f, c.b * 0.58f, 1f); }

    // ---------- Personajes ----------

    /** Humanoide 16x32 mirando a la DERECHA, con contorno y sombreado. */
    private TextureRegion humanoid(Color body, Color accent, boolean shield,
                                   int legPhase, boolean armOut, boolean tucked) {
        int W = 16, H = 32, cx = W / 2;
        Pixmap pm = new Pixmap(W, H, Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        // Piernas (detrás del torso)
        if (tucked) {
            block(pm, cx - 4, 22, 3, 5, SKIN);
            block(pm, cx + 1, 22, 3, 5, SKIN);
        } else {
            switch (legPhase) {
                case 1: block(pm, cx - 5, 22, 3, 9, SKIN); block(pm, cx + 2, 22, 3, 7, SKIN); break;
                case 2: block(pm, cx - 2, 22, 3, 7, SKIN); block(pm, cx + 2, 22, 3, 9, SKIN); break;
                case 3: block(pm, cx + 2, 22, 3, 9, SKIN); block(pm, cx - 5, 22, 3, 7, SKIN); break;
                case 4: block(pm, cx - 4, 23, 3, 8, SKIN); block(pm, cx + 1, 22, 3, 9, SKIN); break;
                default: block(pm, cx - 4, 22, 3, 9, SKIN); block(pm, cx + 1, 22, 3, 9, SKIN);
            }
        }

        // Torso + cinturón
        block(pm, cx - 4, 9, 8, 11, body);
        block(pm, cx - 4, 19, 8, 3, BELT);
        if (shield) block(pm, cx + 3, 10, 2, 9, GOLD);

        // Cabeza, pelo, pluma, ojo
        block(pm, cx - 3, 3, 6, 6, SKIN);
        pm.setColor(HAIR);   pm.fillRectangle(cx - 4, 1, 8, 3);
        pm.setColor(accent); pm.fillRectangle(cx + 2, 0, 3, 3);
        pm.setColor(OUTLINE); pm.fillRectangle(cx + 1, 5, 1, 1);

        // Brazo / arco
        if (armOut) {
            block(pm, cx + 2, 11, 4, 2, SKIN);
            pm.setColor(BOW); pm.fillRectangle(cx + 6, 8, 1, 8);
            pm.fillRectangle(cx + 5, 8, 1, 1); pm.fillRectangle(cx + 5, 15, 1, 1);
        } else {
            block(pm, cx + 3, 10, 2, 7, body);
        }
        return region(pm);
    }

    private Animation<TextureRegion> enemyAnim(Color body, boolean shield) {
        return anim(0.18f,
                humanoid(body, body, shield, 1, false, false),
                humanoid(body, body, shield, 2, false, false));
    }

    /** Boss con contorno + sombreado y ojos brillantes. */
    private TextureRegion boss(int W, int H, Color body, int bob) {
        Pixmap pm = new Pixmap(W, H, Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        int cx = W / 2;
        block(pm, 2, 4 + bob, W - 4, H - 6, body);   // cuerpo
        block(pm, cx - 4, 1 + bob, 8, 6, body);      // cabeza
        pm.setColor(1f, 0.9f, 0.2f, 1f);
        pm.fillRectangle(cx - 3, 3 + bob, 2, 2);
        pm.fillRectangle(cx + 1, 3 + bob, 2, 2);
        return region(pm);
    }

    private TextureRegion arrow(Color c) {
        Pixmap pm = new Pixmap(8, 4, Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(OUTLINE); pm.fillRectangle(0, 0, 8, 4);
        pm.setColor(c);       pm.fillRectangle(0, 1, 6, 2);
        pm.fillRectangle(6, 0, 1, 4); pm.fillRectangle(7, 1, 1, 2);
        return region(pm);
    }

    // ---------- Terreno ----------

    private TextureRegion grassTile(Color grass, Color dirt) {
        int S = 16;
        Pixmap pm = new Pixmap(S, S, Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        // Tierra con motas
        pm.setColor(dirt); pm.fillRectangle(0, 0, S, S);
        pm.setColor(dark(dirt));  pm.fillRectangle(3, 9, 2, 2); pm.fillRectangle(10, 12, 2, 2); pm.fillRectangle(7, 6, 2, 1);
        pm.setColor(light(dirt)); pm.fillRectangle(12, 7, 1, 1); pm.fillRectangle(5, 13, 1, 1);
        // Pasto arriba (fila 0 = superficie superior de la plataforma)
        pm.setColor(grass);       pm.fillRectangle(0, 0, S, 5);
        pm.setColor(light(grass)); pm.fillRectangle(0, 0, S, 1);
        pm.setColor(dark(grass));  pm.fillRectangle(0, 5, S, 1);
        pm.setColor(grass);        pm.fillRectangle(2, 5, 1, 2); pm.fillRectangle(8, 5, 1, 2); pm.fillRectangle(13, 5, 1, 2);
        return region(pm);
    }

    private TextureRegion stoneTile(Color base) {
        int S = 16;
        Pixmap pm = new Pixmap(S, S, Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(base);        pm.fillRectangle(0, 0, S, S);
        pm.setColor(light(base)); pm.fillRectangle(0, 0, S, 2);       // borde superior iluminado
        pm.setColor(dark(base));  pm.fillRectangle(0, S - 2, S, 2);   // sombra inferior
        pm.setColor(dark(base));  pm.fillRectangle(5, 5, 6, 1); pm.fillRectangle(10, 9, 1, 5); pm.fillRectangle(2, 11, 4, 1); // grietas
        return region(pm);
    }

    private TextureRegion buildWhite() {
        Pixmap pm = new Pixmap(1, 1, Format.RGBA8888);
        pm.setColor(Color.WHITE); pm.fill();
        return region(pm);
    }

    // ---------- Utilidades ----------

    private TextureRegion region(Pixmap pm) {
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(t);
        pm.dispose();
        return new TextureRegion(t); // sin volteo (v=0 = top en libGDX)
    }

    private Animation<TextureRegion> anim(float frameDur, TextureRegion... frames) {
        Animation<TextureRegion> a = new Animation<>(frameDur, frames);
        a.setPlayMode(Animation.PlayMode.LOOP);
        return a;
    }

    /** Dibuja una región centrada en (cxPx, cyPx) respetando la mirada. */
    public static void draw(SpriteBatch b, TextureRegion r, float cxPx, float cyPx, boolean faceRight) {
        float w = r.getRegionWidth();
        float h = r.getRegionHeight();
        if (!faceRight) r.flip(true, false);
        b.draw(r, cxPx - w / 2f, cyPx - h / 2f, w, h);
        if (!faceRight) r.flip(true, false);
    }

    // ---------- Getters ----------

    public TextureRegion playerFrame(float t, boolean onGround, boolean moving, boolean shooting) {
        if (shooting) return playerShoot.getKeyFrame(t);
        if (!onGround) return playerJump.getKeyFrame(0f);
        if (moving)    return playerRun.getKeyFrame(t);
        return playerIdle.getKeyFrame(t);
    }

    public TextureRegion enemyFrame(Enemy.Type type, float t, boolean moving) {
        return enemyWalk[type.ordinal()].getKeyFrame(moving ? t : 0f);
    }

    public TextureRegion bossFrame(Boss.Type type, float t) {
        return (type == Boss.Type.XOCOTL ? bossXocotl : bossVargas).getKeyFrame(t);
    }

    public TextureRegion getArrow(boolean isPlayer) { return isPlayer ? arrowPlayer : arrowEnemy; }
    public TextureRegion getTile(int world) { return tiles[(world >= 1 && world <= 4) ? world : 1]; }
    public TextureRegion getWhite() { return white; }

    public void dispose() {
        for (Texture t : textures) t.dispose();
        textures.clear();
    }
}
