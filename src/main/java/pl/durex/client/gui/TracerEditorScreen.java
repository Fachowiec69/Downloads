package pl.durex.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pl.durex.client.module.CustomTracerStyle;
import pl.durex.client.module.TracerModule;

import java.util.ArrayList;
import java.util.List;

public class TracerEditorScreen extends Screen {

    // kolory
    private static final int C_PANEL  = 0xFF0A001A;
    private static final int C_BORDER = 0xFF8800EE;
    private static final int C_ACCENT = 0xFFCC77FF;
    private static final int C_TEXT   = 0xFFEEDDFF;
    private static final int C_MUTED  = 0xFF9966BB;
    private static final int C_ON     = 0xFF44FF88;
    private static final int C_OFF    = 0xFFFF4455;

    // canvas
    private int cx, cy, cw, ch;
    private boolean drawing;
    private int lastX = -1, lastY = -1;
    private final List<int[]> strokes = new ArrayList<>();

    // kolor rysowania
    private float r = 0.53f, g = 0f, b = 0.93f, a = 0.9f;
    private String dragSlider = null;

    // presety
    private static final int[] PRESETS = {
        0xFFAA00FF, 0xFFFF4444, 0xFF44FF88, 0xFFFFCC44,
        0xFFFFFFFF, 0xFF44AAFF, 0xFFFF66CC, 0xFFFF8800, 0xFF00FFFF
    };

    // nazwa
    private final StringBuilder name = new StringBuilder("Custom");
    private boolean editName = false;

    // zapis
    private boolean saved = false;
    private long saveTime = 0;

    // layout side
    private int sx, sw;

    public TracerEditorScreen() {
        super(Text.literal("Custom Tracer Editor"));
    }

    @Override
    protected void init() {
        int pw = Math.min(width - 60, 580);
        int ph = Math.min(height - 60, 400);
        int panelX = (width - pw) / 2;
        int panelY = (height - ph) / 2;

        cx = panelX + 12;
        cy = panelY + 36;
        cw = pw - 190;
        ch = ph - 50;

        sx = cx + cw + 12;
        sw = pw - cw - 36;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        // 1. Ciemne tło pełnoekranowe
        ctx.fillGradient(0, 0, width, height, 0xDD000000, 0xDD000000);

        int pw = Math.min(width - 60, 580);
        int ph = Math.min(height - 60, 400);
        int panelX = (width - pw) / 2;
        int panelY = (height - ph) / 2;

        // 2. Panel
        ctx.fillGradient(panelX, panelY, panelX + pw, panelY + ph, 0xFF0D0020, 0xFF060012);
        rect(ctx, panelX, panelY, pw, ph, C_BORDER);

        // 3. Header
        ctx.fillGradient(panelX, panelY, panelX + pw, panelY + 30, 0xFF1A0040, 0xFF0D0025);
        rect(ctx, panelX, panelY, pw, 30, 0x44AA44FF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("✏  Custom Tracer Editor"), panelX + pw / 2, panelY + 11, C_ACCENT);

        // 4. Canvas tło
        ctx.fillGradient(cx, cy, cx + cw, cy + ch, 0xFF060010, 0xFF040008);
        rect(ctx, cx, cy, cw, ch, 0xFF8800EE);

        // 5. Siatka
        for (int gx = cx + 20; gx < cx + cw; gx += 20)
            ctx.fill(gx, cy + 1, gx + 1, cy + ch - 1, 0x11FFFFFF);
        for (int gy = cy + 20; gy < cy + ch; gy += 20)
            ctx.fill(cx + 1, gy, cx + cw - 1, gy + 1, 0x11FFFFFF);

        // 6. Osie
        int midX = cx + cw / 2, midY = cy + ch / 2;
        ctx.fill(cx + 1, midY, cx + cw - 1, midY + 1, 0x33FFFFFF);
        ctx.fill(midX, cy + 1, midX + 1, cy + ch - 1, 0x33FFFFFF);

        // 7. Origin crosshair
        ctx.fill(midX - 5, midY, midX + 6, midY + 1, 0xCCFFFFFF);
        ctx.fill(midX, midY - 5, midX + 1, midY + 6, 0xCCFFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§8origin"), midX, midY + 7, 0x55FFFFFF);

        // 8. Narysowane kreski
        int col = argb(r, g, b, a);
        for (int[] s : strokes)
            line(ctx, s[0], s[1], s[2], s[3], col);

        // 9. Podgląd bieżącej kreski
        if (drawing && lastX >= 0) {
            int cmx = clamp(mx, cx, cx + cw - 1);
            int cmy = clamp(my, cy, cy + ch - 1);
            line(ctx, lastX, lastY, cmx, cmy, col);
        }

        // 10. Hint pod canvasem
        ctx.drawTextWithShadow(textRenderer,
            Text.literal("§8LPM = rysuj  |  PPM = cofnij ostatnią kreskę"),
            cx, cy + ch + 5, 0x55FFFFFF);

        // 11. Panel boczny
        renderSide(ctx, mx, my, panelY, ph);

        // 12. Komunikat po zapisie
        if (saved) {
            long el = System.currentTimeMillis() - saveTime;
            if (el < 2500) {
                float al = el < 1800 ? 1f : 1f - (el - 1800) / 700f;
                ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§a✔ Successfully added to tracer styles!"),
                    width / 2, panelY + ph + 10, (int)(al * 255) << 24 | 0x44FF88);
            } else {
                saved = false;
                close();
            }
        }
    }

    private void renderSide(DrawContext ctx, int mx, int my, int panelY, int ph) {
        int y = cy;

        // Podgląd koloru
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Kolor:"), sx, y, C_MUTED);
        y += 10;
        ctx.fill(sx, y, sx + sw, y + 18, argb(r, g, b, 1f));
        rect(ctx, sx, y, sw, 18, 0x44FFFFFF);
        y += 22;

        // Presety
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Presety:"), sx, y, C_MUTED);
        y += 10;
        int ps = (sw - 8) / 5;
        for (int i = 0; i < PRESETS.length; i++) {
            int ppx = sx + (i % 5) * (ps + 2);
            int ppy = y + (i / 5) * (ps + 2);
            ctx.fill(ppx, ppy, ppx + ps, ppy + ps, PRESETS[i] | 0xFF000000);
            if (mx >= ppx && mx < ppx + ps && my >= ppy && my < ppy + ps)
                rect(ctx, ppx, ppy, ps, ps, 0xFFFFFFFF);
        }
        y += ((PRESETS.length - 1) / 5 + 1) * (ps + 2) + 6;

        // Slidery
        y = slider(ctx, mx, my, sx, y, sw, "R", r, 0xFFFF4444, "r");
        y = slider(ctx, mx, my, sx, y, sw, "G", g, 0xFF44FF44, "g");
        y = slider(ctx, mx, my, sx, y, sw, "B", b, 0xFF4466FF, "b");
        y = slider(ctx, mx, my, sx, y, sw, "A", a, 0xFFAAAAAA, "a");
        y += 6;

        // Nazwa
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Nazwa:"), sx, y, C_MUTED);
        y += 10;
        boolean nh = mx >= sx && mx < sx + sw && my >= y && my < y + 16;
        ctx.fill(sx, y, sx + sw, y + 16, editName ? 0x33220044 : (nh ? 0x22110033 : 0x11110022));
        rect(ctx, sx, y, sw, 16, editName ? C_BORDER : 0x44AA44FF);
        ctx.drawTextWithShadow(textRenderer,
            Text.literal(name + (editName ? "§8|" : "")), sx + 4, y + 4, C_TEXT);
        y += 20 + 4;

        // Przyciski
        int bw = (sw - 4) / 2;
        boolean ch = mx >= sx && mx < sx + bw && my >= y && my < y + 22;
        ctx.fill(sx, y, sx + bw, y + 22, ch ? 0x44440010 : 0x22220010);
        rect(ctx, sx, y, bw, 22, ch ? C_OFF : 0x44FF4455);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§cClear"), sx + bw / 2, y + 7, C_OFF);

        int saveX = sx + bw + 4;
        boolean sh = mx >= saveX && mx < saveX + bw && my >= y && my < y + 22;
        ctx.fill(saveX, y, saveX + bw, y + 22, sh ? 0x44004420 : 0x22002210);
        rect(ctx, saveX, y, bw, 22, sh ? C_ON : 0x4444FF88);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§aAdd"), saveX + bw / 2, y + 7, C_ON);

        y += 28;
        ctx.drawTextWithShadow(textRenderer,
            Text.literal("§8Kresek: " + strokes.size()), sx, y, C_MUTED);

        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§8ESC = zamknij"), sx + sw / 2, panelY + ph - 12, 0x44FFFFFF);
    }

    private int slider(DrawContext ctx, int mx, int my,
            int x, int y, int w, String lbl, float val, int col, String id) {
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7" + lbl), x, y + 3, C_MUTED);
        int bx = x + 14, bw = w - 14;
        ctx.fill(bx, y + 4, bx + bw, y + 10, 0x22FFFFFF);
        int fw = (int)(val * bw);
        if (fw > 0) ctx.fill(bx, y + 4, bx + fw, y + 10, col);
        int hx = bx + fw - 2;
        boolean hov = id.equals(dragSlider) || (mx >= bx && mx < bx + bw && my >= y && my < y + 14);
        ctx.fill(hx, y + 1, hx + 4, y + 13, hov ? 0xFFFFFFFF : 0xBBFFFFFF);
        return y + 16;
    }

    // ── Mouse ─────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Canvas
        if (mx >= cx && mx < cx + cw && my >= cy && my < cy + ch) {
            if (button == 0) {
                drawing = true;
                lastX = (int) mx; lastY = (int) my;
            } else if (button == 1 && !strokes.isEmpty()) {
                strokes.remove(strokes.size() - 1);
            }
            return true;
        }

        // Presety
        int presetY = cy + 10 + 22 + 10;
        int ps = (sw - 8) / 5;
        for (int i = 0; i < PRESETS.length; i++) {
            int ppx = sx + (i % 5) * (ps + 2);
            int ppy = presetY + (i / 5) * (ps + 2);
            if (mx >= ppx && mx < ppx + ps && my >= ppy && my < ppy + ps) {
                int c = PRESETS[i];
                r = ((c >> 16) & 0xFF) / 255f;
                g = ((c >> 8) & 0xFF) / 255f;
                b = (c & 0xFF) / 255f;
                a = 0.9f;
                return true;
            }
        }

        // Slidery
        int slY = presetY + ((PRESETS.length - 1) / 5 + 1) * (ps + 2) + 6;
        for (String id : new String[]{"r","g","b","a"}) {
            int bx = sx + 14, bw = sw - 14;
            if (mx >= bx && mx < bx + bw && my >= slY && my < slY + 14) {
                dragSlider = id;
                applySlider(id, (float)((mx - bx) / bw));
                return true;
            }
            slY += 16;
        }
        slY += 6;

        // Nazwa
        slY += 10;
        if (mx >= sx && mx < sx + sw && my >= slY && my < slY + 16) {
            editName = true; return true;
        } else editName = false;
        slY += 20 + 4;

        // Przyciski
        int bw = (sw - 4) / 2;
        if (mx >= sx && mx < sx + bw && my >= slY && my < slY + 22) {
            strokes.clear(); return true;
        }
        int saveX = sx + bw + 4;
        if (mx >= saveX && mx < saveX + bw && my >= slY && my < slY + 22) {
            doSave(); return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (drawing && button == 0) {
            int nx = clamp((int)mx, cx, cx + cw - 1);
            int ny = clamp((int)my, cy, cy + ch - 1);
            if (lastX >= 0) strokes.add(new int[]{lastX, lastY, nx, ny});
            lastX = nx; lastY = ny;
            return true;
        }
        if (dragSlider != null) {
            int bx = sx + 14, bw = sw - 14;
            applySlider(dragSlider, clampF((float)((mx - bx) / bw), 0f, 1f));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        drawing = false; lastX = -1; lastY = -1; dragSlider = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (editName && chr >= 32 && name.length() < 24) { name.append(chr); return true; }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editName) {
            if (keyCode == 259 && name.length() > 0) name.deleteCharAt(name.length() - 1);
            else if (keyCode == 257 || keyCode == 256) editName = false;
            return true;
        }
        if (keyCode == 256) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // ── Logic ─────────────────────────────────────────────────────────────

    private void applySlider(String id, float t) {
        switch (id) {
            case "r" -> r = t;
            case "g" -> g = t;
            case "b" -> b = t;
            case "a" -> a = Math.max(0.05f, t);
        }
    }

    private void doSave() {
        if (strokes.isEmpty()) return;
        String n = name.toString().trim();
        if (n.isEmpty()) n = "Custom " + (TracerModule.getCustomStyles().size() + 1);
        int midX = cx + cw / 2, midY = cy + ch / 2;
        float scale = Math.min(cw, ch) / 2f;
        List<float[]> segs = new ArrayList<>();
        for (int[] s : strokes)
            segs.add(new float[]{
                (s[0]-midX)/scale, (s[1]-midY)/scale,
                (s[2]-midX)/scale, (s[3]-midY)/scale,
                r, g, b, a
            });
        TracerModule.addCustomStyle(new CustomTracerStyle(n, segs));
        saved = true;
        saveTime = System.currentTimeMillis();
    }

    // ── Draw helpers ──────────────────────────────────────────────────────

    private static void rect(DrawContext ctx, int x, int y, int w, int h, int col) {
        ctx.fill(x,       y,       x + w,     y + 1,     col);
        ctx.fill(x,       y + h-1, x + w,     y + h,     col);
        ctx.fill(x,       y,       x + 1,     y + h,     col);
        ctx.fill(x + w-1, y,       x + w,     y + h,     col);
    }

    private static void line(DrawContext ctx, int x1, int y1, int x2, int y2, int col) {
        int dx = Math.abs(x2-x1), dy = Math.abs(y2-y1);
        if (dx == 0 && dy == 0) { ctx.fill(x1, y1, x1+1, y1+1, col); return; }
        if (dy == 0) { ctx.fill(Math.min(x1,x2), y1, Math.max(x1,x2)+1, y1+1, col); return; }
        if (dx == 0) { ctx.fill(x1, Math.min(y1,y2), x1+1, Math.max(y1,y2)+1, col); return; }
        int sx = x1<x2?1:-1, sy = y1<y2?1:-1, err = dx-dy, x = x1, y = y1;
        while (true) {
            ctx.fill(x, y, x+1, y+1, col);
            if (x == x2 && y == y2) break;
            int e2 = 2*err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 <  dx) { err += dx; y += sy; }
        }
    }

    private static int argb(float r, float g, float b, float a) {
        return ((int)(a*255)&0xFF)<<24|((int)(r*255)&0xFF)<<16|((int)(g*255)&0xFF)<<8|(int)(b*255)&0xFF;
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static float clampF(float v, float lo, float hi) { return Math.max(lo, Math.min(hi, v)); }

    @Override public boolean shouldPause() { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }
}
