package com.unnecessary.kalaharatweaks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.PriorityQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.minecraftforge.fml.common.gameevent.TickEvent.Phase.START;

abstract public class TickQueue {
    private static long tickCounter = 0L;
    private static final PriorityQueue<Entry> queue = new PriorityQueue<>();
    public static void add(long time, World world, BlockPos pos, BiConsumer<World, BlockPos> callback) {
        queue.add(new Entry(time + tickCounter, world, pos, callback));
    }
    @Mod.EventBusSubscriber()
    public static class TickQueueHandler {
        @SubscribeEvent
        public static void onWorldTick(TickEvent.WorldTickEvent event) {
            if(event.phase == START) return;
            while(queue.peek() != null && queue.peek().time == tickCounter) {
                Entry current = queue.poll();
                System.out.println(current.pos);
                current.callback.accept(current.world, current.pos);
            }
            tickCounter++;
        }
    }
    static class Entry implements Comparable<Entry>{
        long time;
        World world;
        BlockPos pos;
        BiConsumer<World, BlockPos> callback;
        Entry (long time, World world, BlockPos pos, BiConsumer<World, BlockPos> callback){
            this.time = time;
            this.world = world;
            this.pos = pos;
            this.callback = callback;
        }
        public int compareTo (Entry other) {
            return (int) (this.time - other.time);
        }
    }
}
