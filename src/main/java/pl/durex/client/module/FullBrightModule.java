package pl.durex.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/**
 * FullBright - maksymalna jasność (gamma 100)
 */
public class FullBrightModule {
    
    private static boolean enabled = false;
    private static double savedGamma = 1.0;
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return;
        
        if (enabled) {
            savedGamma = mc.options.getGamma().getValue();
            setGamma(mc, 100.0);
        } else {
            setGamma(mc, savedGamma);
        }
    }
    
    public static void toggle() {
        setEnabled(!enabled);
    }
    
    // Wywoływane co tick żeby gamma nie była resetowana przez grę
    public static void onTick() {
        if (!enabled) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return;
        
        double current = mc.options.getGamma().getValue();
        if (current != 100.0) {
            setGamma(mc, 100.0);
        }
    }
    
    private static void setGamma(MinecraftClient mc, double value) {
        try {
            // Metoda 1: przez refleksję na polu value w SimpleOption
            SimpleOption<Double> gammaOption = mc.options.getGamma();
            java.lang.reflect.Field[] fields = gammaOption.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object val = field.get(gammaOption);
                if (val instanceof Double) {
                    field.set(gammaOption, value);
                    break;
                }
            }
        } catch (Exception e) {
            try {
                // Metoda 2: bezpośrednio przez setValue jeśli dostępne
                mc.options.getGamma().setValue(value);
            } catch (Exception ignored) {}
        }
    }
}
