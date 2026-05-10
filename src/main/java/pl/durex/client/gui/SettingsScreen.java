package pl.durex.client.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import pl.durex.client.settings.ClientSettings;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Ekran ustawień klienta — motywy i profile configów.
 */
public class SettingsScreen extends Screen {

    private static final int C_PANEL   = 0xFF0D0018;
    private static final int C_BORDER  = 0xFF8800EE;
    private static final int C_ACCENT  = 0xFFCC77FF;
    private static final int C_TEXT    = 0xFFEEDDFF;
    private static final int C_MUTED   = 0xFF9966BB;
    private static final int C_ON      = 0xFF44FF88;
    private static final int C_OFF     = 0xFFFF4455;
    private static final int C_HOVER   = 0x44AA44FF;
    private static final int C_CAT_SEL = 0xFF1A0035;
    private static final int C_CAT_HOV = 0xFF130028;

    private int px, py, pw, ph;

    private static final String[] CATEGORIES = {"Motywy", "Config"};
    private static final String[] CAT_ICONS  = {"🎨", "⊞"};
    private int selectedCat = 0;
    private static final int CAT_W = 100;

    // Feedback
    private String feedbackMsg   = "";
    private int    feedbackColor = 0xFF44FF88;
    private long   feedbackTime  = 0;

    // ── Profile system ────────────────────────────────────────────────────
    // Popup: wpisywanie nazwy profilu
    private boolean showNamePopup = false;
    private String  popupInput    = "";
    private boolean popupCursor   = false;
    private long    popupCursorTime = 0;
    // Tryb popupu: "save" = nowy, "overwrite" = nadpisz istniejący
    private String  popupMode     = "save";
    private String  popupTarget   = ""; // nazwa profilu do nadpisania

    // Lista profili (nazwy plików bez .json)
    private List<String> profiles = new ArrayList<>();
    private int profileScroll = 0;

    // Folder z profilami
    private static Path getProfilesDir() {
        return FabricLoader.getInstance().getGameDir().resolve("durexconfigs");
    }

    public SettingsScreen() {
        super(Text.literal("Client Settings"));
        refreshProfiles();
    }

    private void refreshProfiles() {
        profiles.clear();
        try {
            Path dir = getProfilesDir();
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Files.list(dir)
                .filter(p -> p.toString().endsWith(".json"))
                .map(p -> p.getFileName().toString().replace(".json", ""))
                .sorted()
                .forEach(profiles::add);
        } catch (Exception ignored) {}
    }

    private void saveProfile(String name) {
        try {
            Path dir = getProfilesDir();
            if (!Files.exists(dir)) Files.createDirectories(dir);
            // Najpierw zapisz aktualny stan do głównego pliku
            pl.durex.client.config.DurexConfig.save();
            // Skopiuj do profilu
            Path src  = FabricLoader.getInstance().getGameDir().resolve("durexclient.json");
            Path dest = dir.resolve(name + ".json");
            if (Files.exists(src)) {
                Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                refreshProfiles();
                showFeedback("§a✔ Profil '" + name + "' zapisany!", 0xFF44FF88);
            }
        } catch (Exception e) {
            showFeedback("§c✘ Błąd zapisu: " + e.getMessage(), 0xFFFF4455);
        }
    }

    private void loadProfile(String name) {
        try {
            Path src  = getProfilesDir().resolve(name + ".json");
            Path dest = FabricLoader.getInstance().getGameDir().resolve("durexclient.json");
            if (Files.exists(src)) {
                Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                pl.durex.client.config.DurexConfig.load();
                showFeedback("§b✔ Profil '" + name + "' wczytany!", 0xFF44AAFF);
            }
        } catch (Exception e) {
            showFeedback("§c✘ Błąd wczytania: " + e.getMessage(), 0xFFFF4455);
        }
    }

    private void deleteProfile(String name) {
        try {
            Path p = getProfilesDir().resolve(name + ".json");
            Files.deleteIfExists(p);
            refreshProfiles();
            showFeedback("§e✔ Profil '" + name + "' usunięty!", 0xFFFFAA44);
        } catch (Exception e) {
            showFeedback("§c✘ Błąd usunięcia: " + e.getMessage(), 0xFFFF4455);
        }
    }

    // ── Render ────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        pw = Math.min(width - 60, 620);
        ph = Math.min(height - 60, 460);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        ctx.fillGradient(0, 0, width, height, 0xCC000000, 0xCC000000);

        // Panel
        ctx.fill(px, py, px + pw, py + ph, C_PANEL);
        rect(ctx, px, py, pw, ph, C_BORDER);

        // Header
        ctx.fill(px, py, px + pw, py + 28, 0xFF120028);
        rect(ctx, px, py, pw, 28, 0x44AA44FF);
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("⚙  Client Settings"), px + pw / 2, py + 10, C_ACCENT);

        // Kategorie (lewa kolumna)
        int lx = px + 8;
        int ly = py + 38;
        for (int i = 0; i < CATEGORIES.length; i++) {
            boolean sel = (i == selectedCat);
            boolean hov = mx >= lx && mx < lx + CAT_W && my >= ly && my < ly + 28;
            int bg = sel ? C_CAT_SEL : (hov ? C_CAT_HOV : 0);
            if (bg != 0) ctx.fill(lx, ly, lx + CAT_W, ly + 28, bg);
            if (sel) ctx.fill(lx, ly, lx + 3, ly + 28, C_BORDER);
            ctx.drawTextWithShadow(textRenderer,
                Text.literal(CAT_ICONS[i] + "  " + CATEGORIES[i]),
                lx + 10, ly + 10,
                sel ? C_ACCENT : (hov ? C_TEXT : C_MUTED));
            ly += 30;
        }

        // Separator
        int sepX = px + CAT_W + 16;
        ctx.fill(sepX, py + 38, sepX + 1, py + ph - 8, 0x44FFFFFF);

        // Prawa kolumna
        int rx = sepX + 8;
        int ry = py + 38;
        int rw = pw - CAT_W - 32;

        switch (selectedCat) {
            case 0 -> renderThemes(ctx, mx, my, rx, ry, rw);
            case 1 -> renderConfig(ctx, mx, my, rx, ry, rw);
        }

        // Hint
        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§8ESC = zamknij"),
            px + pw / 2, py + ph - 10, 0x44FFFFFF);

        // Feedback
        if (!feedbackMsg.isEmpty() && System.currentTimeMillis() - feedbackTime < 3000) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(feedbackMsg), px + pw / 2, py + ph - 22, feedbackColor);
        } else if (System.currentTimeMillis() - feedbackTime >= 3000) {
            feedbackMsg = "";
        }

        // Popup nazwy profilu (na wierzchu)
        if (showNamePopup) renderNamePopup(ctx, mx, my);
    }

    // ── Motywy ────────────────────────────────────────────────────────────

    private void renderThemes(DrawContext ctx, int mx, int my, int x, int y, int w) {
        ctx.drawTextWithShadow(textRenderer, Text.literal("§dMotywy"), x, y, C_ACCENT);
        y += 14;

        for (int i = 0; i < ClientSettings.THEMES.length; i++) {
            String themeId   = ClientSettings.THEMES[i];
            String themeName = ClientSettings.THEME_NAMES[i];
            boolean selected = themeId.equals(ClientSettings.selectedTheme);
            boolean hov = mx >= x && mx < x + w && my >= y && my < y + 28;

            ctx.fill(x, y, x + w, y + 28, selected ? 0x33AA44FF : (hov ? C_HOVER : 0x11110022));
            rect(ctx, x, y, w, 28, selected ? C_BORDER : (hov ? 0x44AA44FF : 0x33660099));

            int rx = x + 6, ry = y + 10;
            ctx.fill(rx, ry, rx + 8, ry + 8, selected ? C_ON : 0x44666666);
            if (selected) ctx.fill(rx + 2, ry + 2, rx + 6, ry + 6, 0xFFFFFFFF);

            ctx.drawTextWithShadow(textRenderer, Text.literal(themeName),
                x + 20, y + 10, selected ? C_TEXT : C_MUTED);

            // Gradient podgląd
            int cpx = x + w - 80, cpw = 70, cph = 16;
            int idx = java.util.Arrays.asList(ClientSettings.THEMES).indexOf(themeId);
            if (idx >= 0 && idx < ClientSettings.THEME_COLORS.length) {
                int col1 = ClientSettings.THEME_COLORS[idx][0];
                int col2 = ClientSettings.THEME_COLORS[idx][1];
                for (int p = 0; p < cpw; p++) {
                    float t = (float) p / cpw;
                    int r = (int) (((col1 >> 16) & 0xFF) * (1 - t) + ((col2 >> 16) & 0xFF) * t);
                    int g = (int) (((col1 >> 8)  & 0xFF) * (1 - t) + ((col2 >> 8)  & 0xFF) * t);
                    int b = (int) ((col1 & 0xFF)         * (1 - t) + (col2 & 0xFF)          * t);
                    ctx.fill(cpx + p, y + 6, cpx + p + 1, y + 6 + cph, 0xFF000000 | (r << 16) | (g << 8) | b);
                }
                rect(ctx, cpx, y + 6, cpw, cph, 0x88FFFFFF);
            }
            y += 30;
        }
    }

    // ── Config / Profile system ───────────────────────────────────────────

    private void renderConfig(DrawContext ctx, int mx, int my, int x, int y, int w) {
        // Tytuł
        ctx.drawTextWithShadow(textRenderer, Text.literal("§aProfile Configów"), x, y, C_ACCENT);
        y += 18;

        // Przycisk "Zapisz nowy profil" (lewa połowa)
        int btnW = (w - 6) / 2;
        boolean saveHov = !showNamePopup && mx >= x && mx < x + btnW && my >= y && my < y + 30;
        ctx.fill(x, y, x + btnW, y + 30, saveHov ? 0x44228844 : 0x22114422);
        rect(ctx, x, y, btnW, 30, saveHov ? C_ON : 0x44448866);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§a💾  Zapisz nowy profil..."), x + 8, y + 11, C_ON);

        // Przycisk "Otwórz folder" (prawa połowa)
        int folderX = x + btnW + 6;
        boolean folderHov = !showNamePopup && mx >= folderX && mx < folderX + btnW && my >= y && my < y + 30;
        ctx.fill(folderX, y, folderX + btnW, y + 30, folderHov ? 0x44886622 : 0x22443311);
        rect(ctx, folderX, y, btnW, 30, folderHov ? 0xFFFFAA44 : 0x44886644);
        ctx.drawTextWithShadow(textRenderer, Text.literal("§e📁  Otwórz folder"), folderX + 8, y + 11, 0xFFFFAA44);

        y += 36;

        // Lista profili
        if (profiles.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer,
                Text.literal("§8Brak zapisanych profili"), x + 4, y + 6, 0xFF555555);
            return;
        }

        ctx.drawTextWithShadow(textRenderer, Text.literal("§7Zapisane profile:"), x, y, C_MUTED);
        y += 14;

        int maxVisible = (ph - (y - py) - 20) / 36;
        int start = Math.max(0, Math.min(profileScroll, profiles.size() - maxVisible));

        for (int i = start; i < Math.min(start + maxVisible, profiles.size()); i++) {
            String name = profiles.get(i);
            boolean rowHov = !showNamePopup && mx >= x && mx < x + w && my >= y && my < y + 32;

            ctx.fill(x, y, x + w, y + 32, rowHov ? 0x22AA44FF : 0x11110022);
            rect(ctx, x, y, w, 32, rowHov ? 0x44AA44FF : 0x33440066);

            // Nazwa profilu
            ctx.drawTextWithShadow(textRenderer, Text.literal("§f" + name), x + 10, y + 12, C_TEXT);

            // Przyciski po prawej: [Wczytaj] [Nadpisz] [Usuń]
            int bx = x + w - 4;

            // [Usuń] - czerwony
            bx -= 44;
            boolean delHov = rowHov && mx >= bx && mx < bx + 40 && my >= y + 6 && my < y + 26;
            ctx.fill(bx, y + 6, bx + 40, y + 26, delHov ? 0x88FF2233 : 0x44882233);
            rect(ctx, bx, y + 6, 40, 20, delHov ? C_OFF : 0x44FF4455);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§cUsuń"), bx + 20, y + 12, C_OFF);

            // [Nadpisz] - żółty
            bx -= 52;
            boolean ovHov = rowHov && mx >= bx && mx < bx + 48 && my >= y + 6 && my < y + 26;
            ctx.fill(bx, y + 6, bx + 48, y + 26, ovHov ? 0x88886600 : 0x44443300);
            rect(ctx, bx, y + 6, 48, 20, ovHov ? 0xFFFFCC00 : 0x44FFAA00);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§eNadpisz"), bx + 24, y + 12, 0xFFFFCC44);

            // [Wczytaj] - zielony
            bx -= 52;
            boolean loadHov = rowHov && mx >= bx && mx < bx + 48 && my >= y + 6 && my < y + 26;
            ctx.fill(bx, y + 6, bx + 48, y + 26, loadHov ? 0x88004422 : 0x44002211);
            rect(ctx, bx, y + 6, 48, 20, loadHov ? C_ON : 0x4444FF88);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§aWczytaj"), bx + 24, y + 12, C_ON);

            y += 36;
        }

        // Scroll hint
        if (profiles.size() > maxVisible) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§8↑↓ scroll"), px + pw / 2, py + ph - 22, 0x44FFFFFF);
        }
    }

    // ── Popup wpisywania nazwy ────────────────────────────────────────────

    private void renderNamePopup(DrawContext ctx, int mx, int my) {
        // Przyciemnienie tła
        ctx.fill(0, 0, width, height, 0x88000000);

        int ppw = 320, pph = 110;
        int ppx = (width - ppw) / 2, ppy = (height - pph) / 2;

        ctx.fill(ppx, ppy, ppx + ppw, ppy + pph, 0xFF0A0020);
        rect(ctx, ppx, ppy, ppw, pph, C_BORDER);

        ctx.drawCenteredTextWithShadow(textRenderer,
            Text.literal("§dNazwa profilu"), ppx + ppw / 2, ppy + 10, C_ACCENT);

        // Pole tekstowe
        int fx = ppx + 12, fy = ppy + 30, fw = ppw - 24, fh = 22;
        ctx.fill(fx, fy, fx + fw, fy + fh, 0xFF050015);
        rect(ctx, fx, fy, fw, fh, C_BORDER);

        // Kursor migający
        boolean cur = (System.currentTimeMillis() / 500) % 2 == 0;
        String display = popupInput + (cur ? "§7|" : "");
        ctx.drawTextWithShadow(textRenderer, Text.literal("§f" + display), fx + 4, fy + 7, C_TEXT);

        // Przyciski
        int bw = 100, bh = 24;
        int okX  = ppx + ppw / 2 - bw - 6;
        int canX = ppx + ppw / 2 + 6;
        int bY   = ppy + pph - bh - 10;

        boolean okHov  = mx >= okX  && mx < okX  + bw && my >= bY && my < bY + bh;
        boolean canHov = mx >= canX && mx < canX + bw && my >= bY && my < bY + bh;

        ctx.fill(okX,  bY, okX  + bw, bY + bh, okHov  ? 0x88004422 : 0x44002211);
        rect(ctx, okX,  bY, bw, bh, okHov  ? C_ON : 0x4444FF88);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§aZapisz"), okX + bw / 2, bY + 8, C_ON);

        ctx.fill(canX, bY, canX + bw, bY + bh, canHov ? 0x88440011 : 0x44220008);
        rect(ctx, canX, bY, bw, bh, canHov ? C_OFF : 0x44FF4455);
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§cAnuluj"), canX + bw / 2, bY + 8, C_OFF);
    }

    // ── Mouse / Key ───────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Popup ma priorytet
        if (showNamePopup) {
            return handlePopupClick(mx, my);
        }

        // Kategorie
        int lx = px + 8, ly = py + 38;
        for (int i = 0; i < CATEGORIES.length; i++) {
            if (mx >= lx && mx < lx + CAT_W && my >= ly && my < ly + 28) {
                selectedCat = i;
                return true;
            }
            ly += 30;
        }

        int sepX = px + CAT_W + 16;
        int rx = sepX + 8, ry = py + 38, rw = pw - CAT_W - 32;

        switch (selectedCat) {
            case 0 -> { return handleThemesClick(mx, my, rx, ry, rw); }
            case 1 -> { return handleConfigClick(mx, my, rx, ry, rw); }
        }

        if (mx < px || mx > px + pw || my < py || my > py + ph) {
            close();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    private boolean handlePopupClick(double mx, double my) {
        int ppw = 320, pph = 110;
        int ppx = (width - ppw) / 2, ppy = (height - pph) / 2;
        int bw = 100, bh = 24;
        int okX  = ppx + ppw / 2 - bw - 6;
        int canX = ppx + ppw / 2 + 6;
        int bY   = ppy + pph - bh - 10;

        // OK
        if (mx >= okX && mx < okX + bw && my >= bY && my < bY + bh) {
            confirmPopup();
            return true;
        }
        // Anuluj
        if (mx >= canX && mx < canX + bw && my >= bY && my < bY + bh) {
            showNamePopup = false;
            popupInput = "";
            return true;
        }
        return true; // pochłoń klik
    }

    private void confirmPopup() {
        String name = popupInput.trim()
            .replaceAll("[^a-zA-Z0-9_\\-ąćęłńóśźżĄĆĘŁŃÓŚŹŻ ]", "")
            .trim();
        if (name.isEmpty()) {
            showFeedback("§c✘ Podaj nazwę profilu!", 0xFFFF4455);
            showNamePopup = false;
            popupInput = "";
            return;
        }
        showNamePopup = false;
        popupInput = "";
        saveProfile(name);
    }

    private boolean handleThemesClick(double mx, double my, int x, int y, int w) {
        y += 14;
        for (int i = 0; i < ClientSettings.THEMES.length; i++) {
            if (mx >= x && mx < x + w && my >= y && my < y + 28) {
                ClientSettings.selectedTheme = ClientSettings.THEMES[i];
                pl.durex.client.config.DurexConfig.save();
                return true;
            }
            y += 30;
        }
        return false;
    }

    private boolean handleConfigClick(double mx, double my, int x, int y, int w) {
        y += 18;

        int btnW = (w - 6) / 2;

        // Zapisz nowy profil
        if (mx >= x && mx < x + btnW && my >= y && my < y + 30) {
            popupInput = "";
            popupMode  = "save";
            showNamePopup = true;
            return true;
        }

        // Otwórz folder
        int folderX = x + btnW + 6;
        if (mx >= folderX && mx < folderX + btnW && my >= y && my < y + 30) {
            try {
                Path dir = getProfilesDir();
                if (!Files.exists(dir)) Files.createDirectories(dir);
                java.awt.Desktop.getDesktop().open(dir.toFile());
            } catch (Exception e) {
                // Desktop może nie działać na Linuxie - fallback przez xdg-open
                try {
                    Path dir = getProfilesDir();
                    if (!Files.exists(dir)) Files.createDirectories(dir);
                    new ProcessBuilder("xdg-open", dir.toString()).start();
                } catch (Exception e2) {
                    showFeedback("§c✘ Nie można otworzyć folderu", 0xFFFF4455);
                }
            }
            return true;
        }

        y += 36;

        if (profiles.isEmpty()) return false;

        y += 14; // "Zapisane profile:" label

        int maxVisible = (ph - (y - py) - 20) / 36;
        int start = Math.max(0, Math.min(profileScroll, profiles.size() - maxVisible));

        for (int i = start; i < Math.min(start + maxVisible, profiles.size()); i++) {
            String name = profiles.get(i);
            if (my >= y && my < y + 32) {
                int bx = x + w - 4;

                // [Usuń]
                bx -= 44;
                if (mx >= bx && mx < bx + 40) {
                    deleteProfile(name);
                    return true;
                }
                // [Nadpisz]
                bx -= 52;
                if (mx >= bx && mx < bx + 48) {
                    saveProfile(name); // nadpisz bez pytania
                    return true;
                }
                // [Wczytaj]
                bx -= 52;
                if (mx >= bx && mx < bx + 48) {
                    loadProfile(name);
                    return true;
                }
            }
            y += 36;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hScroll, double vScroll) {
        if (selectedCat == 1) {
            profileScroll = Math.max(0, profileScroll - (int) Math.signum(vScroll));
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showNamePopup) {
            if (keyCode == 256) { // ESC
                showNamePopup = false;
                popupInput = "";
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // Enter
                confirmPopup();
                return true;
            }
            if (keyCode == 259) { // Backspace
                if (!popupInput.isEmpty())
                    popupInput = popupInput.substring(0, popupInput.length() - 1);
                return true;
            }
            return true;
        }
        if (keyCode == 256) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (showNamePopup) {
            if (popupInput.length() < 32 && chr >= 32) {
                popupInput += chr;
            }
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void showFeedback(String msg, int color) {
        feedbackMsg   = msg;
        feedbackColor = color;
        feedbackTime  = System.currentTimeMillis();
    }

    private static void rect(DrawContext ctx, int x, int y, int w, int h, int col) {
        ctx.fill(x,     y,     x + w, y + 1, col);
        ctx.fill(x,     y+h-1, x + w, y + h, col);
        ctx.fill(x,     y,     x + 1, y + h, col);
        ctx.fill(x+w-1, y,     x + w, y + h, col);
    }

    @Override public boolean shouldPause()      { return false; }
    @Override public boolean shouldCloseOnEsc() { return true; }
}
