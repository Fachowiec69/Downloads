package pl.durex.client.module;

import java.util.ArrayList;
import java.util.List;

/**
 * Przechowuje narysowany przez użytkownika custom tracer.
 * Segmenty są znormalizowane do przestrzeni [-1,1] względem środka canvasa.
 *
 * Rendering: wzór jest traktowany jako "kafelek" o szerokości 1 jednostki
 * i powtarzany wzdłuż linii od crosshaira do gracza.
 * Rozmiar kafelka = TILE_PX pikseli na ekranie.
 */
public class CustomTracerStyle {

    public final String name;
    // [x1, y1, x2, y2, r, g, b, a] — współrzędne w przestrzeni [-1,1]
    public final List<float[]> segments;

    // Rozmiar jednego "kafelka" wzorka w pikselach ekranu
    private static final float TILE_PX = 18f;

    public CustomTracerStyle(String name, List<float[]> segments) {
        this.name = name;
        this.segments = new ArrayList<>(segments);
    }

    /**
     * Rysuje tracer jako powtarzający się wzór (tiling) wzdłuż linii x1,y1 → x2,y2.
     *
     * Wzór jest rysowany w lokalnym układzie współrzędnych kafelka:
     *   - oś U = wzdłuż linii (kierunek do gracza)
     *   - oś V = prostopadle do linii
     * Każdy kafelek ma rozmiar TILE_PX × TILE_PX pikseli.
     * Wzór jest skalowany z przestrzeni [-1,1] do [0, TILE_PX].
     */
    public void draw(net.minecraft.client.gui.DrawContext ctx, int x1, int y1, int x2, int y2) {
        if (segments.isEmpty()) return;

        int dx = x2 - x1, dy = y2 - y1;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;

        // Jednostkowe wektory: U = wzdłuż linii, V = prostopadle
        float uX = (float)(dx / len), uY = (float)(dy / len);
        float vX = -uY, vY = uX;

        // Ile kafelków mieści się na długości linii
        int tiles = Math.max(1, (int)(len / TILE_PX));
        float tileLen = (float)(len / tiles); // rzeczywisty rozmiar kafelka w px

        int color = argb(
            segments.isEmpty() ? 0.53f : segments.get(0)[4],
            segments.isEmpty() ? 0f    : segments.get(0)[5],
            segments.isEmpty() ? 0.93f : segments.get(0)[6],
            segments.isEmpty() ? 0.9f  : segments.get(0)[7]
        );

        for (int t = 0; t < tiles; t++) {
            // Punkt startowy tego kafelka na linii
            float tileOriginX = x1 + uX * t * tileLen;
            float tileOriginY = y1 + uY * t * tileLen;

            for (float[] seg : segments) {
                // seg[0..3] = x1,y1,x2,y2 w przestrzeni [-1,1]
                // Mapuj: [-1,1] → [0, tileLen] wzdłuż U, [-1,1] → [-tileLen/2, tileLen/2] wzdłuż V
                float u1 = (seg[0] + 1f) * 0.5f * tileLen;
                float v1 =  seg[1]       * 0.5f * tileLen;
                float u2 = (seg[2] + 1f) * 0.5f * tileLen;
                float v2 =  seg[3]       * 0.5f * tileLen;

                int sx1 = (int)(tileOriginX + uX * u1 + vX * v1);
                int sy1 = (int)(tileOriginY + uY * u1 + vY * v1);
                int sx2 = (int)(tileOriginX + uX * u2 + vX * v2);
                int sy2 = (int)(tileOriginY + uY * u2 + vY * v2);

                int c = argb(seg[4], seg[5], seg[6], seg[7]);
                drawLine(ctx, sx1, sy1, sx2, sy2, c);
            }
        }
    }

    private static void drawLine(net.minecraft.client.gui.DrawContext ctx,
            int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        if (dx == 0 && dy == 0) { ctx.fill(x1, y1, x1+1, y1+1, color); return; }
        if (dy == 0) { ctx.fill(Math.min(x1,x2), y1, Math.max(x1,x2)+1, y1+1, color); return; }
        if (dx == 0) { ctx.fill(x1, Math.min(y1,y2), x1+1, Math.max(y1,y2)+1, color); return; }
        int sx = x1<x2?1:-1, sy = y1<y2?1:-1, err = dx-dy, x = x1, y = y1;
        while (true) {
            ctx.fill(x, y, x+1, y+1, color);
            if (x == x2 && y == y2) break;
            int e2 = 2*err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 <  dx) { err += dx; y += sy; }
        }
    }

    private static int argb(float r, float g, float b, float a) {
        return ((int)(a*255)&0xFF)<<24|((int)(r*255)&0xFF)<<16|((int)(g*255)&0xFF)<<8|(int)(b*255)&0xFF;
    }
}
