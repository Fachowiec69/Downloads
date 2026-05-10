package pl.durex.client.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * AutoShieldBreak - automatycznie atakuje toporem gdy gracz używa tarczy
 */
public class AutoShieldBreakModule {
    
    private static boolean enabled = false;
    private static int delayTicks = 3; // Delay przed powrotem na miecz (w tickach, 3 = ~150ms)
    
    private static int previousSlot = -1;
    private static int ticksSinceHit = 0;
    private static boolean hasHit = false;
    
    public static boolean isEnabled() {
        return enabled;
    }
    
    public static void setEnabled(boolean value) {
        enabled = value;
        if (!enabled) {
            reset();
        }
    }
    
    public static void toggle() {
        setEnabled(!enabled);
    }
    
    public static int getDelayTicks() {
        return delayTicks;
    }
    
    public static void setDelayTicks(int ticks) {
        delayTicks = Math.max(1, Math.min(20, ticks)); // 1-20 ticków (50ms-1s)
    }
    
    private static void reset() {
        previousSlot = -1;
        ticksSinceHit = 0;
        hasHit = false;
    }
    
    /**
     * Wywołaj w tick event
     */
    public static void onTick() {
        if (!enabled) return;
        
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        
        // Jeśli czekamy na powrót do miecza
        if (hasHit) {
            ticksSinceHit++;
            if (ticksSinceHit >= delayTicks) {
                // Wróć na poprzedni slot (miecz)
                if (previousSlot != -1 && previousSlot != mc.player.getInventory().selectedSlot) {
                    mc.player.getInventory().selectedSlot = previousSlot;
                }
                reset();
            }
            return;
        }
        
        // Sprawdź czy patrzymy na gracza z tarczą
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHit = (EntityHitResult) mc.crosshairTarget;
            if (entityHit.getEntity() instanceof PlayerEntity target) {
                
                // Sprawdź czy gracz blokuje tarczą
                if (isUsingShield(target)) {
                    // Znajdź topór
                    int axeSlot = findAxeSlot(mc);
                    if (axeSlot != -1) {
                        // Zapisz obecny slot
                        if (previousSlot == -1) {
                            previousSlot = mc.player.getInventory().selectedSlot;
                        }
                        
                        // Przełącz na topór
                        if (mc.player.getInventory().selectedSlot != axeSlot) {
                            mc.player.getInventory().selectedSlot = axeSlot;
                        }
                        
                        // Zaatakuj
                        if (mc.interactionManager != null) {
                            mc.interactionManager.attackEntity(mc.player, target);
                            mc.player.swingHand(Hand.MAIN_HAND);
                            hasHit = true;
                            ticksSinceHit = 0;
                        }
                    }
                }
            }
        }
    }
    
    private static boolean isUsingShield(PlayerEntity player) {
        // Sprawdź czy gracz blokuje tarczą
        if (!player.isBlocking()) return false;
        
        ItemStack mainHand = player.getStackInHand(Hand.MAIN_HAND);
        ItemStack offHand = player.getStackInHand(Hand.OFF_HAND);
        
        return mainHand.isOf(Items.SHIELD) || offHand.isOf(Items.SHIELD);
    }
    
    private static int findAxeSlot(MinecraftClient mc) {
        if (mc.player == null) return -1;
        
        // Szukaj topora w hotbarze (sloty 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            
            // Sprawdź czy to topór (dowolny)
            if (stack.isOf(Items.WOODEN_AXE) || 
                stack.isOf(Items.STONE_AXE) || 
                stack.isOf(Items.IRON_AXE) || 
                stack.isOf(Items.GOLDEN_AXE) || 
                stack.isOf(Items.DIAMOND_AXE) || 
                stack.isOf(Items.NETHERITE_AXE)) {
                return i;
            }
        }
        
        return -1;
    }
}
