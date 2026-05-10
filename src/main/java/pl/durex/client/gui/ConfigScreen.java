package pl.durex.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Ekran zarządzania profilami konfiguracji.
 * Pliki .durex w config/DurexClient/ aktualnej instancji MC.
 * FabricLoader.getGameDir() zawsze zwraca folder aktualnej instancji —
 * działa poprawnie na każdym launcherze (Prism, MultiMC, vanilla, itp.)
 */
public class ConfigScreen extends Screen {

    private static final int C_PANEL  = 0xFF0D0018;
    private static final int C_BORDER = 0xFF8800EE;
    private static final int C_ACCENT = 0xFFCC77FF;
    private static final int C_TEXT   = 0xFFEEDDFF;
    private static final int C_MUTED  = 0xFF9966BB;
    private static final int C_ON     = 0xFF44FF88;
    private static final int C_OFF    = 0xFFFF4455;

    private int px, py, pw, ph;

    private final List<Path> profiles = new ArrayList<>();

    // Popup "Create New Config"
    private boolean showPopup = false;
    private final StringBuilder popupName = new StringBuilder();

    // Komunikat
    private String msg = null;
    private long msgTime = 0;

    public ConfigScreen() {
        super(Text.literal("Config Profiles"));
        loadProfiles();
    }

    /** Folder config/DurexClient/ w aktualnej instancji MC */
    private Path getConfigDir() {
        Path dir = FabricLoader.getInstance().getGameDir()
            .resolve("config").resolve("DurexClient");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        return dir;
    }

    /** Główny plik konfiguracji aktualnej instancji */
    private Path getMainConfig() {
        return FabricLoader.getInstance().getGameDir().resolve("durexclient.json");
    }

    private void loadProfiles() {
        profiles.clear();
        try (DirectoryStream<Path> s = Files.newDirectoryStream(getConfigDir(), "*.durex")) {
            for (Path p : s) profiles.add(p);
        } catch (IOException ignored) {}
        profiles.sort((a, b) -> a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString()));
    }

    @Override
    protected void init() {
        pw = Math.min(width - 60, 420);
        ph = Math.min(height - 60, 400);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
    }

    // ── Render ────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fillGradient(0, 0, width, height, 0xCC000000, 0xCC000000);

        // Panel
        ctx.fill(px, py, px + pw, py + ph, C_PANEL);
        rect(ctx, px, py, pw, ph, C_BORDER);

        // Header
        ctx.fill(px, py, px + pw, py + 26, 0xFF120028);
        rect(ctx, px, py, pw, 26, 0x44AA44FF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("⚙  Config Profiles"), px + pw / 2, py + 9, C_ACCENT);

        // Config dir info
        String dirStr = "§8" + getConfigDir().toString();
        // Skróć jeśli za długi
        if (dirStr.length() > 60) dirStr = "§8..." + dirStr.substring(dirStr.length() - 57);
        ctx.drawTextWithShadow(textRenderer, Text.literal(dirStr), px + 8, py + 32, C_MUTED);

        int y = py + 48;

        // ── Save current config ───────────────────────────────────────────
        ctx.fill(px + 8, y, px + pw - 8, y + 1, 0x33FFFFFF);
        y += 8;
        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Save current config:"), px + 8, y, C_MUTED);
        y += 14;

        // Przycisk "Save Config"
        boolean sch = !showPopup && mx >= px + 8 && mx < px + pw - 8 && my >= y && my < y + 24;
        ctx.fill(px + 8, y, px + pw - 8, y + 24, sch ? 0x44004420 : 0x22002210);
        rect(ctx, px + 8, y, pw - 16, 24, sch ? C_ON : 0x4444FF88);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§a+ Save as new config..."), px + pw / 2, y + 8, C_ON);
        y += 32;

        // ── Lista profili ─────────────────────────────────────────────────
        ctx.fill(px + 8, y, px + pw - 8, y + 1, 0x33FFFFFF);
        y += 8;
        ctx.drawTextWithShadow(textRenderer,
            Text.literal("§7Saved configs §8(" + profiles.size() + "):"), px + 8, y, C_MUTED);
        y += 14;

        if (profiles.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer,
                Text.literal("§8No configs yet. Save one first!"), px + 14, y + 6, C_MUTED);
            y += 24;
        }

        for (int i = 0; i < profiles.size(); i++) {
            String name = profiles.get(i).getFileName().toString();
            boolean hov = !showPopup && mx >= px + 8 && mx < px + pw - 8 && my >= y && my < y + 24;

            ctx.fill(px + 8, y, px + pw - 8, y + 24, hov ? 0x33AA44FF : 0x11110022);
            rect(ctx, px + 8, y, pw - 16, 24, hov ? C_BORDER : 0x33660099);

            // Ikona
            ctx.drawTextWithShadow(textRenderer, Text.literal("§5⊞"), px + 12, y + 8, C_ACCENT);
            // Nazwa
            ctx.drawTextWithShadow(textRenderer, Text.literal(name), px + 26, y + 8, hov ? C_TEXT : C_MUTED);

            // Delete button
            int dx = px + pw - 34;
            boolean dh = !showPopup && mx >= dx && mx < dx + 24 && my >= y + 4 && my < y + 20;
            ctx.fill(dx, y + 4, dx + 24, y + 20, dh ? 0x44440010 : 0x22220010);
            rect(ctx, dx, y + 4, 24, 16, dh ? C_OFF : 0x44440022);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§c✕"), dx + 12, y + 8, C_OFF);

            y += 28;
        }

        // Hint
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§8ESC = zamknij"),
            px + pw / 2, py + ph - 10, 0x44FFFFFF);

        // Komunikat
        if (msg != null) {
            long el = System.currentTimeMillis() - msgTime;
            if (el < 2500) {
                float al = el < 2000 ? 1f : 1f - (el - 2000) / 500f;
                boolean isErr = msg.startsWith("§c");
                int col = ((int)(al * 255) << 24) | (isErr ? 0xFF4455 : 0x44FF88);
                ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(msg),
                    px + pw / 2, py + ph + 6, col);
            } else msg = null;
        }

        // ── Popup ─────────────────────────────────────────────────────────
        if (showPopup) renderPopup(ctx, mx, my);
    }

    private void renderPopup(DrawContext ctx, int mx, int my) {
        int popW = 300, popH = 110;
        int popX = (width - popW) / 2;
        int popY = (height - popH) / 2;

        // Overlay
        ctx.fill(0, 0, width, height, 0x88000000);

        // Panel
        ctx.fill(popX, popY, popX + popW, popY + popH, 0xFF0D0018);
        rect(ctx, popX, popY, popW, popH, C_BORDER);

        // Tytuł
        ctx.fill(popX, popY, popX + popW, popY + 24, 0xFF120028);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§dSave Config As"), popX + popW / 2, popY + 8, C_ACCENT);

        // Pole tekstowe
        int fy = popY + 32;
        ctx.fill(popX + 10, fy, popX + popW - 10, fy + 20, 0x33220044);
        rect(ctx, popX + 10, fy, popW - 20, 20, C_BORDER);
        ctx.drawTextWithShadow(textRenderer,
            Text.literal(popupName + "§8.durex§7|"),
            popX + 14, fy + 6, C_TEXT);

        // Przyciski
        int bw = (popW - 30) / 2;
        int by = fy + 28;

        // Cancel
        boolean cnh = mx >= popX + 10 && mx < popX + 10 + bw && my >= by && my < by + 22;
        ctx.fill(popX + 10, by, popX + 10 + bw, by + 22, cnh ? 0x44440010 : 0x22220010);
        rect(ctx, popX + 10, by, bw, 22, cnh ? C_OFF : 0x44440022);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§cCancel"), popX + 10 + bw / 2, by + 7, C_OFF);

        // Save
        int sx = popX + 20 + bw;
        boolean sh = mx >= sx && mx < sx + bw && my >= by && my < by + 22;
        ctx.fill(sx, by, sx + bw, by + 22, sh ? 0x44004420 : 0x22002210);
        rect(ctx, sx, by, bw, 22, sh ? C_ON : 0x4444FF88);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§aSave"), sx + bw / 2, by + 7, C_ON);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (showPopup) {
            int popW = 300, popH = 110;
            int popX = (width - popW) / 2;
            int popY = (height - popH) / 2;
            int bw = (popW - 30) / 2;
            int by = popY + 32 + 28;

            if (mx >= popX + 10 && mx < popX + 10 + bw && my >= by && my < by + 22) {
                showPopup = false; popupName.setLength(0); return true; // Cancel
            }
            int sx = popX + 20 + bw;
            if (mx >= sx && mx < sx + bw && my >= by && my < by + 22) {
                saveConfig(); return true; // Save
            }
            if (mx < popX || mx > popX + popW || my < popY || my > popY + popH) {
                showPopup = false; popupName.setLength(0); return true;
            }
            return true;
        }

        // Save button
        int y = py + 48 + 8 + 14;
        if (mx >= px + 8 && mx < px + pw - 8 && my >= y && my < y + 24) {
            showPopup = true; popupName.setLength(0); return true;
        }
        y += 32 + 8 + 14;

        // Profile list
        for (int i = 0; i < profiles.size(); i++) {
            int dx = px + pw - 34;
            if (mx >= dx && mx < dx + 24 && my >= y + 4 && my < y + 20) {
                deleteProfile(i); return true;
            }
            if (mx >= px + 8 && mx < dx && my >= y && my < y + 24) {
                loadProfile(i); return true;
            }
            y += 28;
        }

        if (mx < px || mx > px + pw || my < py || my > py + ph) { close(); return true; }
        return super.mouseClicked(mx, my, button);
    }

    // ── Keyboard ──────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showPopup) {
            if (keyCode == 257 || keyCode == 335) { saveConfig(); return true; }
            if (keyCode == 256) { showPopup = false; popupName.setLength(0); return true; }
            if (keyCode == 259 && popupName.length() > 0) { popupName.deleteCharAt(popupName.length() - 1); return true; }
            return true;
        }
        if (keyCode == 256) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (showPopup && chr >= 32 && popupName.length() < 32
                && chr != '/' && chr != '\\' && chr != ':' && chr != '*'
                && chr != '?' && chr != '"' && chr != '<' && chr != '>' && chr != '|') {
            popupName.append(chr);
            return true;
        }
        return false;
    }

    // ── Logic ─────────────────────────────────────────────────────────────

    private void saveConfig() {
        String name = popupName.toString().trim();
        if (name.isEmpty()) { showMsg("§cPodaj nazwę!"); return; }
        if (!name.endsWith(".durex")) name = name + ".durex";
        try {
            pl.durex.client.config.DurexConfig.save();
            Path src = getMainConfig();
            Path dst = getConfigDir().resolve(name);
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            loadProfiles();
            showPopup = false;
            popupName.setLength(0);
            showMsg("§aSaved: " + name);
        } catch (IOException e) {
            showMsg("§cError: " + e.getMessage());
        }
    }

    private void loadProfile(int idx) {
        try {
            Path src = profiles.get(idx);
            Path dst = getMainConfig();
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            pl.durex.client.config.DurexConfig.load();
            showMsg("§aLoaded: " + src.getFileName());
        } catch (IOException e) {
            showMsg("§cError: " + e.getMessage());
        }
    }

    private void deleteProfile(int idx) {
        try {
            String name = profiles.get(idx).getFileName().toString();
            Files.deleteIfExists(profiles.get(idx));
            loadProfiles();
            showMsg("§cDeleted: " + name);
        } catch (IOException e) {
            showMsg("§cError: " + e.getMessage());
        }
    }

    private void showMsg(String m) { msg = m; msgTime = System.currentTimeMillis(); }

    private static void rect(DrawContext ctx, int x, int y, int w, int h, int col) {
        ctx.fill(x,     y,     x+w,   y+1,   col);
        ctx.fill(x,     y+h-1, x+w,   y+h,   col);
        ctx.fill(x,     y,     x+1,   y+h,   col);
        ctx.fill(x+w-1, y,     x+w,   y+h,   col);
    }

    @Override public boolean shouldPause()      { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }
}
