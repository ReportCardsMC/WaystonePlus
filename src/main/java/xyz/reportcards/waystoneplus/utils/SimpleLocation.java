package xyz.reportcards.waystoneplus.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Objects;

public class SimpleLocation {

    String world;
    long x;
    long y;
    long z;

    public SimpleLocation(String world, long x, long y, long z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static SimpleLocation fromString(String string) {
        String[] parts = string.split(",");
        return new SimpleLocation(parts[0], Long.parseLong(parts[1]), Long.parseLong(parts[2]), Long.parseLong(parts[3]));
    }

    public static SimpleLocation fromBukkitLocation(Location location) {
        return new SimpleLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Location getBukkitLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public double distance(SimpleLocation other) {
        return Math.sqrt(Math.pow(other.x - x, 2) + Math.pow(other.y - y, 2) + Math.pow(other.z - z, 2));
    }

    @Override
    public String toString() {
        return world + "," + x + "," + y + "," + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleLocation that = (SimpleLocation) o;
        return x == that.x && y == that.y && z == that.z && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}
