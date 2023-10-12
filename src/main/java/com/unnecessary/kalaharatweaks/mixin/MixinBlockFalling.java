package com.unnecessary.kalaharatweaks.mixin;

import com.unnecessary.kalaharatweaks.TickQueue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.shuffle;
import static net.minecraft.block.BlockFalling.canFallThrough;

@Mixin(BlockFalling.class)
public class MixinBlockFalling extends Block {
    public MixinBlockFalling() {
        super(Material.SAND);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }
    private static void callback(World world, BlockPos pos_) {
        System.out.println("checking");
        if(canFallThrough(world.getBlockState(pos_))) {
            System.out.println("passed");
            List<EnumFacing> dirs = new ArrayList<>(Arrays.asList(EnumFacing.HORIZONTALS));
            shuffle(dirs);
            for (EnumFacing dir : dirs) {
                IBlockState state = world.getBlockState(pos_.offset(dir));
                if (state.getBlock() instanceof BlockFalling) {
                    world.setBlockToAir(pos_.offset(dir));
                    world.setBlockState(pos_, state);
                    world.spawnEntity(new EntityFallingBlock(world, (double)pos_.getX() + 0.5D, (double)pos_.getY(), (double)pos_.getZ() + 0.5D, state));
                    if(canFallThrough(world.getBlockState(pos_.offset(dir).up()))){
                        TickQueue.add(2L, world, pos_.offset(dir).up(), MixinBlockFalling::callback);
                    }
                    break;
                }
            }
        }
    };

    @Inject(at = @At(value = "HEAD"), method = "checkFallable")
    private void mixinCheckFallable(World worldIn, BlockPos pos, CallbackInfo ci) {
        if (canFallThrough(worldIn.getBlockState(pos.down())) && pos.getY() >= 0 && canFallThrough(worldIn.getBlockState(pos.up()))) {
            TickQueue.add(2L, worldIn, pos.up(), MixinBlockFalling::callback);
        }
    }
}
