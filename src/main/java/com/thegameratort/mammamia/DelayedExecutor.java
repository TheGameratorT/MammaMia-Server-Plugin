package com.thegameratort.mammamia;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class DelayedExecutor
{
    private static class Entry
    {
        Runnable func;
        Integer delay;

        Entry(Runnable func, Integer delay)
        {
            this.func = func;
            this.delay = delay;
        }
    }

    private final ArrayList<Entry> entries = new ArrayList<>();
    private final Plugin plugin;

    public DelayedExecutor(Plugin plugin)
    {
        this.plugin = plugin;
    }

    public void add(Runnable func, int delay)
    {
        this.entries.add(new Entry(func, delay));
    }

    public void runAll()
    {
        this.run(0);
    }

    private void run(int i)
    {
        if (this.entries.size() > i)
        {
            Entry entry = this.entries.get(i);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                entry.func.run();
                this.run(i + 1);
            }, entry.delay);
        }
    }
}
