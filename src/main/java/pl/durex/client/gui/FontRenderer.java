package pl.durex.client.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import pl.durex.client.settings.ClientSettings;

/**
 * Wrapper dla TextRenderer który stosuje wybraną czcionkę Unicode.
 */
public class FontRenderer {
    
    /**
     * Rysuje tekst z wybraną czcionką Unicode.
     */
    public static int drawText(TextRenderer renderer, net.minecraft.client.gui.DrawContext ctx, 
                               String text, int x, int y, int color, boolean shadow) {
        String converted = convertToFont(text);
        Text textComponent = Text.literal(converted);
        
        if (shadow) {
            return ctx.drawTextWithShadow(renderer, textComponent, x, y, color);
        } else {
            return ctx.drawText(renderer, textComponent, x, y, color, false);
        }
    }
    
    /**
     * Konwertuje tekst na wybraną czcionkę używając Unicode Mathematical Alphanumeric Symbols.
     */
    public static String convertToFont(String text, String font) {
        if ("default".equals(font)) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // Zachowaj Minecraft color codes (§x)
            if (c == '§' && i + 1 < text.length()) {
                result.append(c);
                result.append(text.charAt(i + 1));
                i++; // Skip next char
                continue;
            }
            
            // Konwertuj tylko litery i cyfry
            char converted = convertChar(c, font);
            result.append(converted);
        }
        
        return result.toString();
    }
    
    /**
     * Konwertuje tekst na wybraną czcionkę (używa ClientSettings.selectedFont).
     */
    private static String convertToFont(String text) {
        return convertToFont(text, ClientSettings.selectedFont);
    }
    
    /**
     * Konwertuje pojedynczy znak na odpowiedni Unicode dla czcionki.
     */
    private static char convertChar(char c, String font) {
        return switch (font) {
            case "script" -> convertScript(c);      // 𝒟𝓊𝓇𝑒𝓍
            case "bold" -> convertBoldScript(c);    // 𝓓𝓾𝓻𝓮𝔁
            case "fraktur" -> convertFraktur(c);    // 𝔇𝔲𝔯𝔢𝔵
            case "double" -> convertDouble(c);      // 𝔻𝕦𝕣𝕖𝕩
            case "sans" -> convertSans(c);          // 𝖣𝗎𝗋𝖾𝗑
            case "mono" -> convertMono(c);          // 𝙳𝚞𝚛𝚎𝚡
            default -> c;
        };
    }
    
    // Script (Elegant) - 𝒟𝓊𝓇𝑒𝓍
    private static char convertScript(char c) {
        if (c >= 'A' && c <= 'Z') return (char)(0x1D49C + (c - 'A'));
        if (c >= 'a' && c <= 'z') return (char)(0x1D4B6 + (c - 'a'));
        return c;
    }
    
    // Bold Script - 𝓓𝓾𝓻𝓮𝔁
    private static char convertBoldScript(char c) {
        if (c >= 'A' && c <= 'Z') return (char)(0x1D4D0 + (c - 'A'));
        if (c >= 'a' && c <= 'z') return (char)(0x1D4EA + (c - 'a'));
        return c;
    }
    
    // Fraktur (Gothic) - 𝔇𝔲𝔯𝔢𝔵
    private static char convertFraktur(char c) {
        if (c >= 'A' && c <= 'Z') return (char)(0x1D504 + (c - 'A'));
        if (c >= 'a' && c <= 'z') return (char)(0x1D51E + (c - 'a'));
        return c;
    }
    
    // Double-Struck - 𝔻𝕦𝕣𝕖𝕩
    private static char convertDouble(char c) {
        if (c >= 'A' && c <= 'Z') return (char)(0x1D538 + (c - 'A'));
        if (c >= 'a' && c <= 'z') return (char)(0x1D552 + (c - 'a'));
        if (c >= '0' && c <= '9') return (char)(0x1D7D8 + (c - '0'));
        return c;
    }
    
    // Sans-Serif - 𝖣𝗎𝗋𝖾𝗑
    private static char convertSans(char c) {
        if (c >= 'A' && c <= 'Z') return (char)(0x1D5A0 + (c - 'A'));
        if (c >= 'a' && c <= 'z') return (char)(0x1D5BA + (c - 'a'));
        if (c >= '0' && c <= '9') return (char)(0x1D7E2 + (c - '0'));
        return c;
    }
    
    // Monospace - 𝙳𝚞𝚛𝚎𝚡
    private static char convertMono(char c) {
        if (c >= 'A' && c <= 'Z') return (char)(0x1D670 + (c - 'A'));
        if (c >= 'a' && c <= 'z') return (char)(0x1D68A + (c - 'a'));
        if (c >= '0' && c <= '9') return (char)(0x1D7F6 + (c - '0'));
        return c;
    }
    
    /**
     * Oblicza szerokość tekstu z wybraną czcionką.
     */
    public static int getWidth(TextRenderer renderer, String text) {
        String converted = convertToFont(text);
        return renderer.getWidth(converted);
    }
}
