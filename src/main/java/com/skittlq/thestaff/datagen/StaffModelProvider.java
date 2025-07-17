package com.skittlq.thestaff.datagen;

import com.google.gson.*;
import com.skittlq.thestaff.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.skittlq.thestaff.util.ModConstants.ALLOWED_ITEMS;

// TODO: Use Data Gen to get Minecraft block models instead of hardcoding block in template.
public class StaffModelProvider implements DataProvider {

    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public StaffModelProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return lookupProvider.thenCompose(provider -> {
            TagKey<Item> tag = ModTags.Items.ALLOWED_BLOCKS;
            Set<Item> allowedItems = new HashSet<>(ALLOWED_ITEMS);

            if (allowedItems.isEmpty()) {
                System.out.println("⚠ No items found in tag: " + tag.location());
                return CompletableFuture.completedFuture(null);
            }

            JsonObject template = loadTemplateModel();
            JsonArray cases = new JsonArray();
            List<CompletableFuture<?>> tasks = new ArrayList<>();

            for (Item item : allowedItems) {
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                String blockName = id.getPath();
                String texturePath = id.getNamespace() + ":block/" + blockName;

                JsonObject model = deepCopy(template);
                model.getAsJsonObject("textures").addProperty("1", texturePath);

                String modelName = "purple_staff_" + blockName;
                Path modelPath = output.getOutputFolder().resolve("assets/thestaff/models/item/" + modelName + ".json");
                tasks.add(DataProvider.saveStable(cache, model, modelPath));

                JsonObject selectCase = new JsonObject();
                selectCase.addProperty("when", id.toString());

                JsonObject caseModel = new JsonObject();
                caseModel.addProperty("type", "minecraft:model");
                caseModel.addProperty("model", "thestaff:item/" + modelName);

                selectCase.add("model", caseModel);
                cases.add(selectCase);
            }

            JsonObject clientItem = new JsonObject();

            JsonObject modelSection = new JsonObject();
            modelSection.addProperty("type", "minecraft:select");
            modelSection.addProperty("property", "thestaff:stored_block_id");

            JsonObject fallback = new JsonObject();
            fallback.addProperty("type", "minecraft:model");
            fallback.addProperty("model", "thestaff:item/purple_staff_empty");

            modelSection.add("fallback", fallback);
            modelSection.add("cases", cases);
            clientItem.add("model", modelSection);

            Path itemJsonPath = output.getOutputFolder().resolve("assets/thestaff/items/purple_staff.json");
            tasks.add(DataProvider.saveStable(cache, clientItem, itemJsonPath));

            return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Staff Block Models";
    }

    private JsonObject loadTemplateModel() {
        try (InputStream stream = StaffModelProvider.class.getResourceAsStream("/model_templates/purple_staff.json")) {
            if (stream == null) throw new IllegalStateException("Missing model template");
            return JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException("Error loading template model", e);
        }
    }

    private JsonObject deepCopy(JsonObject original) {
        return JsonParser.parseString(original.toString()).getAsJsonObject();
    }
}
