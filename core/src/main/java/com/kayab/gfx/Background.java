package com.kayab.gfx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.kayab.Constants;

/**
 * US — Mejora visual: fondos con PARALLAX por capas, generados por código
 * (Pixmap, sin PNGs). Cada mundo tiene su set:
 *   1 Floresta · 2 Selva + gris metálico · 3 Tenochtitlan ardiendo · 4 Aldea gris.
 *
 * Render en espacio de pantalla (320x180): cada capa se desplaza a una
 * fracción de la cámara (factor) para dar profundidad, y se repite en X
 * con costura invisible (perfiles seno con número entero de ondas).
 */
public class Background {

    private static final int W = Constants.VIRTUAL_WIDTH;   // 320
    private static final int H = Constants.VIRTUAL_HEIGHT;   // 180

    private static class Layer {
        final TextureRegion region;
        final float factor;   // 0 = fijo (cielo), 1 = se mueve con la cámara
        final boolean tileX;  // repetir en horizontal
        Layer(TextureRegion r, float factor, boolean tileX) {
            this.region = r; this.factor = factor; this.tileX = tileX;
        }
    }

    private final Array<Texture> textures = new Array<>();
    private final Layer[][] worlds = new Layer[5][]; // índice = nº de mundo (1..4)
    private final Matrix4 screenProj = new Matrix4().setToOrtho2D(0, 0, W, H);

    public Background() {
        worlds[1] = buildForest();
        worlds[2] = buildJungleIron();
        worlds[3] = buildBurningEmpire();
        worlds[4] = buildGrayVillage();
    }

    // ===================== MUNDOS =====================

    private Layer[] buildForest() {
        return new Layer[] {
            new Layer(sky(c(0.45f,0.75f,0.92f), c(0.86f,0.92f,0.78f)), 0f, false),
            new Layer(silhouette(c(0.40f,0.55f,0.50f), 74, 16, 2, 3, 0.7), 0.15f, true),  // montañas
            new Layer(silhouette(c(0.16f,0.40f,0.20f), 96, 24, 5, 8, 1.3), 0.40f, true),  // arboleda
            new Layer(silhouette(c(0.07f,0.23f,0.12f), 150, 16, 7, 11, 0.3), 0.78f, true) // follaje cercano
        };
    }

    private Layer[] buildJungleIron() {
        return new Layer[] {
            new Layer(sky(c(0.52f,0.60f,0.58f), c(0.72f,0.74f,0.68f)), 0f, false),
            new Layer(silhouette(c(0.42f,0.48f,0.50f), 74, 16, 2, 3, 0.4), 0.15f, true),
            new Layer(silhouette(c(0.20f,0.34f,0.24f), 96, 22, 5, 8, 1.0), 0.40f, true),
            new Layer(silhouette(c(0.11f,0.19f,0.15f), 150, 15, 7, 11, 0.6), 0.78f, true)
        };
    }

    private Layer[] buildBurningEmpire() {
        return new Layer[] {
            new Layer(sky(c(0.42f,0.14f,0.07f), c(0.88f,0.46f,0.12f)), 0f, false),
            new Layer(silhouette(c(0.26f,0.12f,0.10f), 82, 14, 2, 4, 0.9), 0.15f, true),  // humo lejano
            new Layer(pyramids(c(0.14f,0.08f,0.07f), 152), 0.40f, true),                   // pirámides
            new Layer(silhouette(c(0.07f,0.05f,0.05f), 152, 10, 9, 13, 0.2), 0.78f, true)  // ruinas cercanas
        };
    }

    private Layer[] buildGrayVillage() {
        return new Layer[] {
            new Layer(sky(c(0.30f,0.34f,0.40f), c(0.56f,0.59f,0.63f)), 0f, false),
            new Layer(silhouette(c(0.34f,0.36f,0.40f), 76, 14, 2, 3, 0.5), 0.15f, true),
            new Layer(crossesLayer(c(0.20f,0.21f,0.24f), c(0.12f,0.12f,0.14f), 104, 12), 0.40f, true),
            new Layer(silhouette(c(0.13f,0.14f,0.16f), 150, 12, 7, 11, 0.8), 0.78f, true)
        };
    }

    // ===================== BUILDERS =====================

    /** Cielo: degradado vertical opaco a pantalla completa. */
    private TextureRegion sky(Color top, Color bottom) {
        Pixmap pm = new Pixmap(W, H, Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        for (int y = 0; y < H; y++) {
            float t = y / (float) (H - 1); // 0 arriba, 1 abajo
            pm.setColor(lerp(top, bottom, t));
            pm.fillRectangle(0, y, W, 1);
        }
        return region(pm);
    }

    /** Silueta ondulada (montañas, arboledas, follaje). Rellena de la cresta
     *  hacia abajo. Las ondas usan nº entero de ciclos → repite sin costura. */
    private TextureRegion silhouette(Color col, int crestY, int amp, int fA, int fB, double phase) {
        Pixmap pm = new Pixmap(W, H, Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(col);
        for (int x = 0; x < W; x++) {
            double t = (double) x / W;
            double s = 0.5 * Math.sin(2 * Math.PI * fA * t) + 0.5 * Math.sin(2 * Math.PI * fB * t + phase);
            int top = crestY + (int) Math.round(amp * s);
            if (top < 0) top = 0; if (top > H) top = H;
            pm.fillRectangle(x, top, 1, H - top);
        }
        return region(pm);
    }

    /** Pirámides escalonadas sobre una base sólida (Mundo 3). */
    private TextureRegion pyramids(Color col, int baseY) {
        Pixmap pm = new Pixmap(W, H, Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(col);
        pm.fillRectangle(0, baseY, W, H - baseY); // base de tierra
        int[] cx = {70, 165, 255};
        int[] hw = {48, 62, 40};
        int[] ht = {72, 96, 56};
        Color edge = new Color(col.r * 1.4f, col.g * 1.4f, col.b * 1.4f, 1f);
        for (int x = 0; x < W; x++) {
            int best = 0;
            for (int i = 0; i < cx.length; i++) {
                int d = Math.abs(x - cx[i]);
                if (d <= hw[i]) {
                    int hgt = (int) (ht[i] * (1f - (float) d / hw[i]));
                    // escalonado: cuantizar a peldaños de 8px
                    hgt = (hgt / 8) * 8;
                    if (hgt > best) best = hgt;
                }
            }
            if (best > 0) {
                pm.setColor(col);
                pm.fillRectangle(x, baseY - best, 1, best);
            }
        }
        // arista superior iluminada (apex de cada pirámide)
        pm.setColor(edge);
        for (int i = 0; i < cx.length; i++) pm.fillRectangle(cx[i] - 2, baseY - ((ht[i] / 8) * 8), 4, 3);
        return region(pm);
    }

    /** Banda baja + cruces (Mundo 4: la aldea profanada). */
    private TextureRegion crossesLayer(Color band, Color cross, int crestY, int amp) {
        Pixmap pm = new Pixmap(W, H, Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        // banda ondulada baja
        pm.setColor(band);
        for (int x = 0; x < W; x++) {
            double t = (double) x / W;
            double s = 0.5 * Math.sin(2 * Math.PI * 3 * t) + 0.5 * Math.sin(2 * Math.PI * 5 * t + 0.4);
            int top = crestY + (int) Math.round(amp * s);
            if (top < 0) top = 0; if (top > H) top = H;
            pm.fillRectangle(x, top, 1, H - top);
        }
        // cruces sobresaliendo de la banda
        pm.setColor(cross);
        int[] xs = {40, 120, 210, 290};
        for (int cxp : xs) {
            int topY = crestY - 24;
            pm.fillRectangle(cxp - 1, topY, 3, 30);      // poste
            pm.fillRectangle(cxp - 6, topY + 7, 15, 3);  // travesaño
        }
        return region(pm);
    }

    // ===================== RENDER =====================

    /** Dibuja el fondo del mundo (1..4). Autocontenido: hace su propio begin/end. */
    public void render(SpriteBatch batch, int world, float cameraX) {
        Layer[] layers = worlds[(world >= 1 && world <= 4) ? world : 1];
        batch.setProjectionMatrix(screenProj);
        batch.begin();
        batch.setColor(Color.WHITE);
        for (Layer L : layers) {
            if (!L.tileX) {
                batch.draw(L.region, 0, 0, W, H); // cielo a pantalla completa
                continue;
            }
            float w = L.region.getRegionWidth();
            float off = -(cameraX * L.factor) % w;
            if (off > 0) off -= w;
            for (float x = off; x < W; x += w) batch.draw(L.region, x, 0, w, H);
        }
        batch.end();
    }

    // ===================== UTILIDADES =====================

    private Color c(float r, float g, float b) { return new Color(r, g, b, 1f); }

    private Color lerp(Color a, Color b, float t) {
        return new Color(a.r + (b.r - a.r) * t, a.g + (b.g - a.g) * t, a.b + (b.b - a.b) * t, 1f);
    }

    private TextureRegion region(Pixmap pm) {
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        textures.add(t);
        pm.dispose();
        return new TextureRegion(t);
    }

    public void dispose() {
        for (Texture t : textures) t.dispose();
        textures.clear();
    }
}
