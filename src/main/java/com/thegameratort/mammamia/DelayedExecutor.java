package com.thegameratort.mammamia;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public class DelayedExecutor {
    public static class Event {
        private boolean cancelled = false;

        public void cancel() {
            this.cancelled = true;
        }
    }

    public interface Callback {
        void run(Event e);
    }

    private static class Entry {
        Callback func;
        Integer delay;

        Entry(Callback func, Integer delay) {
            this.func = func;
            this.delay = delay;
        }
    }

    private final ArrayList<Entry> entries = new ArrayList<>();
    private final Plugin plugin;

    public DelayedExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    public void add(Callback func, int delay) {
        this.entries.add(new Entry(func, delay));
    }

    public void runAll() {
        this.run(0);
    }

    private void run(int i) {
        if (this.entries.size() > i) {
            Entry entry = this.entries.get(i);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                Event event = new Event();
                entry.func.run(event);
                if (!event.cancelled) {
                    this.run(i + 1);
                }
            }, entry.delay);
        }
    }
}
