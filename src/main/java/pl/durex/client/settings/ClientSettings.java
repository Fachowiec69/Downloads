package pl.durex.client.settings;

/**
 * Globalne ustawienia klienta — dźwięki, czcionki i motywy.
 */
public class ClientSettings {
    
    // ── Dźwięki ───────────────────────────────────────────────────────────
    public static String selectedSound = "asmr_click";
    public static float soundVolume = 0.5f;
    
    public static final String[] SOUNDS = {
        "asmr_click",      // Kliknięcie ASMR
        "asmr_hover",      // Najechanie myszką
        "asmr_toggle_on",  // Włączenie modułu
        "asmr_toggle_off", // Wyłączenie modułu
        "asmr_whoosh",     // Przesunięcie
        "asmr_pop",        // Pop
        "asmr_tap",        // Delikatne stukanie
        "asmr_swoosh",     // Swoosh
        "asmr_ding",       // Dzwonek
        "asmr_soft"        // Miękki dźwięk
    };
    
    public static final String[] SOUND_NAMES = {
        "Click", "Hover", "Toggle On", "Toggle Off", "Whoosh",
        "Pop", "Tap", "Swoosh", "Ding", "Soft"
    };
    
    // ── Czcionki ──────────────────────────────────────────────────────────
    public static String selectedFont = "default";
    
    public static final String[] FONTS = {
        "default", "script", "bold", "fraktur", "double", "sans", "mono"
    };
    
    public static final String[] FONT_NAMES = {
        "Minecraft Default",
        "Script (Elegant)",
        "Bold (Pogrubiona)",
        "Fraktur (Gothic)",
        "Double-Struck",
        "Sans-Serif",
        "Monospace"
    };
    
    public static final String[] FONT_PREVIEWS = {
        "Durex Client",
        "𝒟𝓊𝓇𝑒𝓍 𝒞𝓁𝒾𝑒𝓃𝓉",
        "𝓓𝓾𝓻𝓮𝔁 𝓒𝓵𝓲𝓮𝓷𝓽",
        "𝔇𝔲𝔯𝔢𝔵 ℭ𝔩𝔦𝔢𝔫𝔱",
        "𝔻𝕦𝕣𝕖𝕩 ℂ𝕝𝕚𝕖𝕟𝕥",
        "𝖣𝗎𝗋𝖾𝗑 𝖢𝗅𝗂𝖾𝗇𝗍",
        "𝙳𝚞𝚛𝚎𝚡 𝙲𝚕𝚒𝚎𝚗𝚝"
    };
    
    // ── Motywy (Themes) ───────────────────────────────────────────────────
    public static String selectedTheme = "purple_dark";
    
    public static final String[] THEMES = {
        "purple_dark",    // Domyślny fioletowy ciemny
        "discord_dark",   // Discord ciemny (szaro-niebieski)
        "discord_blurple",// Discord Blurple (fioletowo-niebieski)
        "sunset",         // Gradient pomarańczowo-różowy
        "ocean",          // Gradient niebiesko-cyjan
        "fire",           // Gradient czerwono-pomarańczowy
        "candy",          // Gradient różowo-fioletowy
        "midnight",       // Ciemny niebieski
        "amoled"          // Czarny AMOLED
    };
    
    public static final String[] THEME_NAMES = {
        "Purple Dark", "Discord Dark", "Discord Blurple", "Sunset", "Ocean",
        "Fire", "Candy", "Midnight", "AMOLED"
    };
    
    // Kolory motywów (gradient: primary -> secondary)
    public static final int[][] THEME_COLORS = {
        {0xFF8800EE, 0xFFCC77FF}, // Purple Dark
        {0xFF5865F2, 0xFF7289DA}, // Discord Dark (blurple)
        {0xFF5865F2, 0xFF57F287}, // Discord Blurple + green
        {0xFFFF6B6B, 0xFFFFD93D}, // Sunset (red -> yellow)
        {0xFF4ECDC4, 0xFF556270}, // Ocean (cyan -> blue)
        {0xFFFF416C, 0xFFFF4B2B}, // Fire (red gradient)
        {0xFFFF6FD8, 0xFF3813C2}, // Candy (pink -> purple)
        {0xFF2C3E50, 0xFF3498DB}, // Midnight (dark blue)
        {0xFF000000, 0xFF1A1A1A}  // AMOLED (black)
    };
    
    // ── Theme Colors ──────────────────────────────────────────────────────
    public static class Theme {
        public final int bg, panel, border, accent, text, muted, on, off;
        
        public Theme(int bg, int panel, int border, int accent, int text, int muted, int on, int off) {
            this.bg = bg; this.panel = panel; this.border = border; this.accent = accent;
            this.text = text; this.muted = muted; this.on = on; this.off = off;
        }
    }
    
    public static Theme getTheme() {
        return switch (selectedTheme) {
            case "discord_dark" -> new Theme(
                0xFF2C2F33, 0xFF36393F, 0xFF5865F2, 0xFF7289DA, 
                0xFFDCDDDE, 0xFF99AAB5, 0xFF57F287, 0xFFED4245
            );
            case "discord_blurple" -> new Theme(
                0xFF202225, 0xFF2F3136, 0xFF5865F2, 0xFF7289DA, 
                0xFFFFFFFF, 0xFFB9BBBE, 0xFF57F287, 0xFFED4245
            );
            case "sunset" -> new Theme(
                0xFF1A0A0A, 0xFF2A1515, 0xFFFF6B6B, 0xFFFFD93D, 
                0xFFFFEEDD, 0xFFFFAA77, 0xFF57F287, 0xFFED4245
            );
            case "ocean" -> new Theme(
                0xFF0A1520, 0xFF152535, 0xFF4ECDC4, 0xFF556270, 
                0xFFCCFFFF, 0xFF88CCDD, 0xFF57F287, 0xFFED4245
            );
            case "fire" -> new Theme(
                0xFF200A0A, 0xFF351515, 0xFFFF416C, 0xFFFF4B2B, 
                0xFFFFDDDD, 0xFFFF8888, 0xFF57F287, 0xFFED4245
            );
            case "candy" -> new Theme(
                0xFF1A0A20, 0xFF2A1535, 0xFFFF6FD8, 0xFF3813C2, 
                0xFFFFDDFF, 0xFFDD88FF, 0xFF57F287, 0xFFED4245
            );
            case "midnight" -> new Theme(
                0xFF1A1F2E, 0xFF2C3E50, 0xFF3498DB, 0xFF5DADE2, 
                0xFFECF0F1, 0xFFBDC3C7, 0xFF57F287, 0xFFED4245
            );
            case "amoled" -> new Theme(
                0xFF000000, 0xFF0A0A0A, 0xFF444444, 0xFF888888, 
                0xFFCCCCCC, 0xFF666666, 0xFF44FF88, 0xFFFF4455
            );
            default -> new Theme( // purple_dark
                0xFF08000F, 0xFF0D0018, 0xFF8800EE, 0xFFCC77FF, 
                0xFFEEDDFF, 0xFF9966BB, 0xFF44FF88, 0xFFFF4455
            );
        };
    }
    
    // ── Metody pomocnicze ─────────────────────────────────────────────────
    
    public static void playSound(String soundId) {
        try {
            net.minecraft.client.MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
            if (mc.player == null) return;
            
            // Mapowanie na istniejące dźwięki Minecrafta (działają!)
            net.minecraft.sound.SoundEvent sound = switch (soundId) {
                case "asmr_click" -> net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value();
                case "asmr_hover" -> net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_HARP.value();
                case "asmr_toggle_on" -> net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
                case "asmr_toggle_off" -> net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_BASS.value();
                case "asmr_whoosh" -> net.minecraft.sound.SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA.value();
                case "asmr_pop" -> net.minecraft.sound.SoundEvents.ENTITY_CHICKEN_EGG;
                case "asmr_tap" -> net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value();
                case "asmr_swoosh" -> net.minecraft.sound.SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP;
                case "asmr_ding" -> net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_BELL.value();
                case "asmr_soft" -> net.minecraft.sound.SoundEvents.BLOCK_WOOL_BREAK;
                default -> net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value();
            };
            
            mc.player.playSound(sound, soundVolume, 1.0f);
        } catch (Exception e) {
            System.err.println("[ClientSettings] Failed to play sound: " + e.getMessage());
        }
    }
    
    public static net.minecraft.util.Identifier getFontIdentifier() {
        return switch (selectedFont) {
            case "fraktur" -> net.minecraft.util.Identifier.of("durexclient", "fraktur");
            case "inter" -> net.minecraft.util.Identifier.of("durexclient", "inter");
            case "mono" -> net.minecraft.util.Identifier.of("durexclient", "mono");
            case "elegant" -> net.minecraft.util.Identifier.of("durexclient", "elegant");
            default -> net.minecraft.util.Identifier.ofVanilla("default");
        };
    }
}
