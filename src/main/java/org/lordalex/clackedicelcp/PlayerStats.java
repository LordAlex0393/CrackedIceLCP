package org.lordalex.clackedicelcp;

public class PlayerStats {
    private int kills = 0;
    private int gifts = 0;
    private int chests = 0;
    private int gold = 0;
    private int lives = 3;

    public int getKills() { return kills; }
    public void addKill() { this.kills++; }

    public int getGifts() { return gifts; }
    public void addGift() { this.gifts++; }

    public int getChests() { return chests; }
    public void addChest() { this.chests++; }

    public int getGold() { return gold; }
    public void setGold(int gold) { this.gold = gold; }
    public void addGold(int amount) { this.gold += amount; }

    public int getLives() { return lives; }
    public void removeLife() { if (lives > 0) lives--; }
    public void setLives(int lives) { this.lives = lives; }
}