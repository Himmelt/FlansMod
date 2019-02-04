package co.uk.flansmods.client.model;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public class RenderSpawnerBlock implements ISimpleBlockRenderingHandler {

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID,
                                     RenderBlocks renderer) {

    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
                                    Block block, int modelId, RenderBlocks renderer) {
        renderer.renderBlockAllFaces(block, x, y, z);
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory() {
        return false;
    }

    @Override
    public int getRenderId() {
        return 0;
    }

}
