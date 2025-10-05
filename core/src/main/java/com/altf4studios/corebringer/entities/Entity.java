package com.altf4studios.corebringer.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import com.altf4studios.corebringer.status.Poison;

public abstract class Entity implements BattleEntity {
    protected String name;
    protected int maxHealth;
    protected int health;
    protected int attack;
    protected int defense;
    protected boolean alive = true;
    protected int block = 0; // Block points, absorbs damage before HP
    protected Map<String, Integer> statusEffects = new HashMap<>(); // Status effects (e.g., "Vulnerable", "Weakened")
    protected List<Poison> poisonEffects = new ArrayList<>(); // Poison status effects

    public Entity(String name, int maxHealth, int attack, int defense) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.attack = attack;
        this.defense = defense;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHealth() {
        return health;
    }

    /// This is for obtaining HP for usage
    public int getHp() {
        return getHealth(); /// This delegates it to existing method
    }

    public void setHp(int hp) {
        this.health = Math.max(0, Math.min(hp, maxHealth)); /// Prevents HP from going beyond Max HP
        if (this.health == 0) {
            alive = false;
            onDeath();
        }
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Set the maximum health for this entity and clamp current health to the new max.
     * Useful when replacing or rerolling enemies so their max HP can be updated at runtime.
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = Math.max(1, maxHealth);
        if (this.health > this.maxHealth) this.health = this.maxHealth;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getBlock() {
        return block;
    }

    /**
     * Explicitly set current block value (shield). Clamped to >= 0.
     */
    public void setBlock(int value) {
        this.block = Math.max(0, value);
    }

    /**
     * Convenience to clear all current block.
     */
    public void clearBlock() {
        this.block = 0;
    }

    // Status management
    public void addStatus(String status, int value) {
        int current = statusEffects.getOrDefault(status, 0);
        statusEffects.put(status, Math.max(0, current + Math.max(1, value)));
    }

    public void removeStatus(String status) {
        statusEffects.remove(status);
    }

    public boolean hasStatus(String status) {
        return statusEffects.containsKey(status) && statusEffects.get(status) > 0;
    }

    // Poison management
    public void addPoison(Poison poison) {
        if (poison != null) {
            // Merge into a single poison stack so total stacks decrease by 1 per turn
            if (poisonEffects.isEmpty()) {
                poisonEffects.add(poison);
                poison.onApply();
            } else {
                Poison existing = poisonEffects.get(0);
                existing.increasePower(poison.getPower());
                existing.onApply();
            }
        }
    }

    public void removePoison(Poison poison) {
        if (poison != null) {
            poisonEffects.remove(poison);
        }
    }

    public List<Poison> getPoisonEffects() {
        return new ArrayList<>(poisonEffects);
    }

    public boolean hasPoison() {
        return !poisonEffects.isEmpty();
    }

    public int getTotalPoisonPower() {
        int totalPower = 0;
        for (Poison poison : poisonEffects) {
            totalPower += poison.getPower();
        }
        return totalPower;
    }

    /**
     * Apply all poison effects and remove expired ones
     * This should be called at the start of each turn
     */
    public void applyPoisonEffects() {
        List<Poison> expiredPoisons = new ArrayList<>();

        for (Poison poison : poisonEffects) {
            poison.applyPoisonDamage(this);
            if (poison.isExpired()) {
                expiredPoisons.add(poison);
            }
        }

        // Remove expired poison effects
        for (Poison expiredPoison : expiredPoisons) {
            poisonEffects.remove(expiredPoison);
        }
    }

    // Block gain and modifiers
    public void gainBlock(int amount) {
        block += applyBlockModifiers(amount);
    }

    public int applyBlockModifiers(int baseBlock) {
        double modified = baseBlock;
        if (hasStatus("Fortified")) {
            modified *= 2;
        }
        // Add more block-related statuses here if needed
        return Math.max(0, (int)Math.round(modified));
    }

    // Damage modifiers
    public int applyDamageModifiers(Entity target, int baseDamage) {
        double modified = baseDamage;
        if (target.hasStatus("Vulnerable")) {
            modified *= 1.5;
        }
        if (this.hasStatus("Weakened")) {
            modified *= 0.75;
        }
        // Add more damage-related statuses here if needed
        return Math.max(0, (int)Math.round(modified));
    }

    // Deal damage (with modifiers)
    public void dealDamage(Entity target, int amount) {
        int finalDamage = applyDamageModifiers(target, amount);
        target.takeDamage(finalDamage);
    }

    @Override
    public void takeDamage(int amount) {
        int blocked = Math.min(block, amount);
        int hpLoss = amount - blocked;
        block -= blocked;
        if (block < 0) block = 0;
        health -= hpLoss;
        if (health < 0) health = 0;
        if (health == 0) {
            alive = false;
            onDeath();
        }
    }

    protected void onDeath() {
        // Default death behavior, can be overridden
    }

    @Override
    public void applyStatus() {
        // Default: do nothing. Override in subclasses for status effects.
    }

    // Generic status helpers for named statuses (e.g., "Stun")
    public int getStatusValue(String status) {
        Integer v = statusEffects.get(status);
        return v != null ? v : 0;
    }

    public void decrementStatus(String status, int amount) {
        int current = getStatusValue(status);
        int next = Math.max(0, current - Math.max(1, amount));
        if (next == 0) {
            statusEffects.remove(status);
        } else {
            statusEffects.put(status, next);
        }
    }

    // Targeting methods to be implemented in subclasses
}
