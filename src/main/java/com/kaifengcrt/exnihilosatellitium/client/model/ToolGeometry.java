package com.kaifengcrt.exnihilosatellitium.client.model;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.Set;
import java.util.function.Function;

/**
 * Delegating geometry so the loader can pick TiC's tool model or the plain
 * layered fallback at read time while keeping one registered loader id.
 */
public final class ToolGeometry implements IUnbakedGeometry<ToolGeometry> {

    private final IUnbakedGeometry<?> delegate;

    public ToolGeometry(final IUnbakedGeometry<?> delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public BakedModel bake(final IGeometryBakingContext owner, final ModelBaker baker,
                           final Function<Material, TextureAtlasSprite> spriteGetter,
                           final ModelState modelState, final ItemOverrides overrides,
                           final ResourceLocation modelLocation) {
        return ((IUnbakedGeometry) delegate)
                .bake(owner, baker, spriteGetter, modelState, overrides, modelLocation);
    }

    @Override
    public void resolveParents(final Function<ResourceLocation, UnbakedModel> modelGetter,
                               final IGeometryBakingContext context) {
        delegate.resolveParents(modelGetter, context);
    }

    @Override
    public Set<String> getConfigurableComponentNames() {
        return delegate.getConfigurableComponentNames();
    }
}
