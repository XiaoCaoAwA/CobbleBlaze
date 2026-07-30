package xiaocaoawa.minecraft.mod.cobbleblaze.burner;

import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/**
 * Lightweight, client-safe description of the Cobblemon occupying a burner: just what the
 * renderer needs (species + aspects) plus the species' base scale. The full Pokémon data is kept
 * server-side only (see {@code BlazeBurnerBlockEntityMixin}) so it round-trips intact on retrieval.
 */
public final class CobblemonOccupant {
    public final ResourceLocation species;
    public final Set<String> aspects;
    public final float baseScale;

    public CobblemonOccupant(ResourceLocation species, Set<String> aspects, float baseScale) {
        this.species = species;
        this.aspects = aspects;
        this.baseScale = baseScale;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Species", species.toString());
        ListTag list = new ListTag();
        for (String aspect : aspects) {
            list.add(StringTag.valueOf(aspect));
        }
        tag.put("Aspects", list);
        tag.putFloat("BaseScale", baseScale);
        return tag;
    }

    public static CobblemonOccupant load(CompoundTag tag) {
        ResourceLocation species = ResourceLocation.parse(tag.getString("Species"));
        Set<String> aspects = new LinkedHashSet<>();
        ListTag list = tag.getList("Aspects", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            aspects.add(list.getString(i));
        }
        float baseScale = tag.contains("BaseScale") ? tag.getFloat("BaseScale") : 1.0F;
        return new CobblemonOccupant(species, aspects, baseScale);
    }
}
