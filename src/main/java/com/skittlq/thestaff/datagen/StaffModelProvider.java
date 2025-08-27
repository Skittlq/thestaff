package com.skittlq.thestaff.datagen;

import com.google.gson.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.skittlq.thestaff.util.ModConstants.ALLOWED_ITEMS;

public class StaffModelProvider implements DataProvider {
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final ResourceManager resourceManager;

    public StaffModelProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ResourceManager resourceManager) {
        this.output = output;
        this.lookupProvider = lookupProvider;
        this.resourceManager = resourceManager;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return lookupProvider.thenCompose(provider -> {
            Set<Item> allowedItems = new HashSet<>(ALLOWED_ITEMS);

            ResourceLocation emptyStaffModelLoc = ResourceLocation.fromNamespaceAndPath("thestaff", "models/item/purple_staff.json");
            JsonObject staffTemplate = loadModel(emptyStaffModelLoc);

            if (staffTemplate == null) {
                System.out.println("[StaffModelProvider] Could not find empty staff model at: " + emptyStaffModelLoc);
                return CompletableFuture.completedFuture(null);
            }

            float blockOffsetX = 4, blockOffsetY = 19, blockOffsetZ = 4;
            float socketSize = 8f; // The size for [4,19,4]→[12,27,12] (all axes)
            List<CompletableFuture<?>> tasks = new ArrayList<>();

            for (Item item : allowedItems) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

                // 1. Load blockstate
                ResourceLocation blockstateLoc = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "blockstates/" + itemId.getPath() + ".json");
                JsonObject blockstate = loadJson(blockstateLoc);
                if (blockstate == null) {
                    System.out.println("[StaffModelProvider] No blockstate for " + itemId);
                    continue;
                }

                // 2. Get referenced model(s) (default/false conditions)
                Set<ResourceLocation> modelLocs = getDefaultModelLocations(blockstate, itemId);
                if (modelLocs.isEmpty()) {
                    System.out.println("[StaffModelProvider] No models found in blockstate for " + itemId);
                    continue;
                }

                // 3. Flatten and merge models
                MergedModel merged = new MergedModel();
                for (ResourceLocation modelLoc : modelLocs) {
                    mergeModelRecursive(modelLoc, merged, new HashSet<>());
                }
                if (merged.elements.size() == 0) {
                    System.out.println("[StaffModelProvider] No elements to merge for " + itemId);
                    continue;
                }

                // 4. Scale & offset all elements into socket
                scaleAndOffsetElementsToSocket(merged.elements, blockOffsetX, blockOffsetY, blockOffsetZ, socketSize);

                // 5. Unify textures for faces
                unifyFaceTexturesSmart(merged.textures, merged.elements);

                // 6. Compose with staff template
                JsonObject outModel = deepCopy(staffTemplate);
                JsonObject outTextures = outModel.getAsJsonObject("textures");
                // Merge in textures, non-destructively
                for (Map.Entry<String, JsonElement> entry : merged.textures.entrySet()) {
                    if (!outTextures.has(entry.getKey())) outTextures.add(entry.getKey(), entry.getValue());
                }
                // Merge in elements
                JsonArray outElements = outModel.getAsJsonArray("elements");
                for (JsonElement e : merged.elements) outElements.add(e);

                // 7. Save
                String modelName = "purple_staff_" + itemId.getPath();
                Path modelPath = output.getOutputFolder().resolve("assets/thestaff/models/item/" + modelName + ".json");
                tasks.add(DataProvider.saveStable(cache, outModel, modelPath));
                System.out.println("[StaffModelProvider] Generated merged model for " + itemId + " as " + modelName);
            }

// --- After generating merged models ---

            JsonObject selector = new JsonObject();
            selector.addProperty("type", "minecraft:select");
            selector.addProperty("property", "thestaff:stored_block_id");

            JsonArray cases = new JsonArray();
            for (Item item : allowedItems) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

                JsonObject caseObj = new JsonObject();
                caseObj.addProperty("when", itemId.toString());

                JsonObject modelObj = new JsonObject();
                modelObj.addProperty("type", "minecraft:model");
                modelObj.addProperty("model", "thestaff:item/purple_staff_" + itemId.getPath());

                caseObj.add("model", modelObj);
                cases.add(caseObj);
            }
            selector.add("cases", cases);

            JsonObject fallback = new JsonObject();
            fallback.addProperty("type", "minecraft:model");
            fallback.addProperty("model", "thestaff:item/purple_staff_empty");
            selector.add("fallback", fallback);

// Top-level object
            JsonObject out = new JsonObject();
            out.add("model", selector);

            Path selectorModelPath = output.getOutputFolder().resolve("assets/thestaff/items/purple_staff.json");
            tasks.add(DataProvider.saveStable(cache, out, selectorModelPath));

            return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
        });
    }

    // === UTILITIES ===

    private JsonObject loadModel(ResourceLocation loc) {
        return loadJson(loc);
    }
    private JsonObject loadJson(ResourceLocation loc) {
        Optional<Resource> res = resourceManager.getResource(loc);
        if (res.isPresent()) {
            try (InputStream s = res.get().open()) {
                return JsonParser.parseReader(new InputStreamReader(s)).getAsJsonObject();
            } catch (IOException e) {
                System.out.println("[StaffModelProvider] IOException reading " + loc + ": " + e.getMessage());
            }
        }
        return null;
    }

    // --- Blockstate Parsing ---
    private Set<ResourceLocation> getDefaultModelLocations(JsonObject blockstate, ResourceLocation itemId) {
        Set<ResourceLocation> result = new LinkedHashSet<>();
        if (blockstate.has("variants")) {
            JsonObject variants = blockstate.getAsJsonObject("variants");
            // Prefer "" key, else use the most "default-looking" key
            if (variants.has("")) {
                // Unchanged logic for empty key
                JsonElement v = variants.get("");
                collectVariantModelLocations(v, itemId, result);
            } else if (variants.size() > 0) {
                // Try to select by key for cake, grass_block, etc.
                String key = null;
                if (variants.has("bites=0")) key = "bites=0";
                else if (variants.has("snowy=false")) key = "snowy=false";
                else key = variants.keySet().iterator().next(); // fallback: first
                JsonElement v = variants.get(key);
                collectVariantModelLocations(v, itemId, result);
            }
        } else if (blockstate.has("multipart")) {
            JsonArray multipart = blockstate.getAsJsonArray("multipart");
            for (JsonElement e : multipart) {
                JsonObject entry = e.getAsJsonObject();
                boolean use = true;
                if (entry.has("when")) {
                    JsonObject when = entry.getAsJsonObject("when");
                    for (Map.Entry<String, JsonElement> cond : when.entrySet()) {
                        // Use if explicitly "false"
                        if (!"false".equals(cond.getValue().getAsString())) {
                            use = false;
                        }
                    }
                }
                if (!use) continue;
                JsonObject apply = entry.getAsJsonObject("apply");
                String modelStr = apply.get("model").getAsString();
                result.add(modelLocationFromBlock(modelStr, itemId));
            }
        }
        return result;
    }

    private void collectVariantModelLocations(JsonElement v, ResourceLocation itemId, Set<ResourceLocation> result) {
        if (v.isJsonObject()) {
            String modelStr = v.getAsJsonObject().get("model").getAsString();
            result.add(modelLocationFromBlock(modelStr, itemId));
        } else if (v.isJsonArray()) {
            JsonArray arr = v.getAsJsonArray();
            if (arr.size() > 0) {
                String modelStr = arr.get(0).getAsJsonObject().get("model").getAsString();
                result.add(modelLocationFromBlock(modelStr, itemId));
            }
        }
    }

    // Returns model location relative to its block/item root
    private ResourceLocation modelLocationFromBlock(String modelStr, ResourceLocation fallbackNS) {
        String[] parts = modelStr.split(":");
        String ns = (parts.length > 1) ? parts[0] : fallbackNS.getNamespace();
        String path = (parts.length > 1) ? parts[1] : parts[0];
        return ResourceLocation.fromNamespaceAndPath(ns, "models/" + path + ".json");
    }

    // --- Model Flatten & Merge ---
    private static class MergedModel {
        JsonObject textures = new JsonObject();
        JsonArray elements = new JsonArray();
    }

    private void mergeModelRecursive(ResourceLocation modelLoc, MergedModel merged, Set<ResourceLocation> visited) {
        if (visited.contains(modelLoc)) return;
        visited.add(modelLoc);

        JsonObject model = loadModel(modelLoc);
        if (model == null) return;

        // Merge parent first
        if (model.has("parent")) {
            String parentPath = model.get("parent").getAsString();
            ResourceLocation parentLoc = resolveParentLoc(parentPath, modelLoc);
            mergeModelRecursive(parentLoc, merged, visited);
        }
        // Merge textures
        if (model.has("textures")) {
            JsonObject tex = model.getAsJsonObject("textures");
            for (Map.Entry<String, JsonElement> e : tex.entrySet()) {
                if (!merged.textures.has(e.getKey()))
                    merged.textures.add(e.getKey(), e.getValue());
            }
        }
        // Merge elements
        if (model.has("elements")) {
            JsonArray elems = model.getAsJsonArray("elements");
            for (JsonElement e : elems) merged.elements.add(deepCopy(e));
        }
    }

    private ResourceLocation resolveParentLoc(String parentPath, ResourceLocation modelLoc) {
        if (parentPath.contains(":")) {
            String[] parts = parentPath.split(":", 2);
            return ResourceLocation.fromNamespaceAndPath(parts[0], "models/" + parts[1] + ".json");
        } else {
            // Relative: same namespace, "models/" prefix
            String ns = modelLoc.getNamespace();
            return ResourceLocation.fromNamespaceAndPath(ns, "models/" + parentPath + ".json");
        }
    }

    private void scaleAndOffsetElementsToSocket(JsonArray elements, float offsetX, float offsetY, float offsetZ, float socketSize) {
        // Find original bounds
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE, maxZ = Float.MIN_VALUE;
        for (JsonElement elem : elements) {
            JsonObject cube = elem.getAsJsonObject();
            float[] from = toFloatArray(cube.getAsJsonArray("from"));
            float[] to = toFloatArray(cube.getAsJsonArray("to"));
            minX = Math.min(minX, Math.min(from[0], to[0]));
            minY = Math.min(minY, Math.min(from[1], to[1]));
            minZ = Math.min(minZ, Math.min(from[2], to[2]));
            maxX = Math.max(maxX, Math.max(from[0], to[0]));
            maxY = Math.max(maxY, Math.max(from[1], to[1]));
            maxZ = Math.max(maxZ, Math.max(from[2], to[2]));
        }
        float sizeX = maxX - minX, sizeY = maxY - minY, sizeZ = maxZ - minZ;
        float scale = socketSize / Math.max(Math.max(sizeX, sizeY), sizeZ);

        // Now, bottom-anchored! Align minY to offsetY, but keep aspect
        for (JsonElement elem : elements) {
            JsonObject cube = elem.getAsJsonObject();
            float[] from = toFloatArray(cube.getAsJsonArray("from"));
            float[] to = toFloatArray(cube.getAsJsonArray("to"));
            for (int i = 0; i < 3; ++i) {
                from[i] = (from[i] - ((i==1) ? minY : (i==0) ? minX : minZ)) * scale + ((i==1) ? offsetY : (i==0) ? offsetX : offsetZ);
                to[i]   = (to[i]   - ((i==1) ? minY : (i==0) ? minX : minZ)) * scale + ((i==1) ? offsetY : (i==0) ? offsetX : offsetZ);
            }
            cube.add("from", floatArrayToJson(from));
            cube.add("to", floatArrayToJson(to));
            resetAllFaceUVs(cube);
        }
    }

    // --- Utility: Set all faces' "uv" to [0,0,16,16] ---
    private void resetAllFaceUVs(JsonObject cube) {
        if (!cube.has("faces")) return;
        JsonObject faces = cube.getAsJsonObject("faces");
        JsonArray uv = floatArrayToJson(new float[] {0, 0, 16, 16});
        for (String face : Arrays.asList("down", "up", "north", "south", "east", "west")) {
            if (faces.has(face)) {
                JsonObject faceObj = faces.getAsJsonObject(face);
                faceObj.add("uv", deepCopy(uv));
                faceObj.remove("rotation");
            }
        }
    }

    // --- Texture mapping logic (unchanged, but "smarter") ---
    private void unifyFaceTexturesSmart(JsonObject blockTextures, JsonArray elements) {
        boolean hasAll = blockTextures.has("all");
        boolean hasSide = blockTextures.has("side");
        boolean hasFront = blockTextures.has("front");
        boolean hasBack = blockTextures.has("back");
        boolean hasTop = blockTextures.has("top");
        boolean hasBottom = blockTextures.has("bottom");

        for (JsonElement elem : elements) {
            JsonObject cube = elem.getAsJsonObject();
            if (!cube.has("faces")) continue;
            JsonObject faces = cube.getAsJsonObject("faces");

            for (String face : Arrays.asList("down", "up", "north", "south", "east", "west")) {
                if (faces.has(face)) {
                    JsonObject faceObj = faces.getAsJsonObject(face);
                    if (hasFront && hasBack && hasSide) {
                        switch (face) {
                            case "north": faceObj.addProperty("texture", "#front"); break;
                            case "south": faceObj.addProperty("texture", "#back"); break;
                            default: faceObj.addProperty("texture", "#side"); break;
                        }
                    } else if (hasAll) {
                        faceObj.addProperty("texture", "#all");
                    } else if (hasSide && hasTop && hasBottom) {
                        switch (face) {
                            case "up": faceObj.addProperty("texture", "#top"); break;
                            case "down": faceObj.addProperty("texture", "#bottom"); break;
                            default: faceObj.addProperty("texture", "#side"); break;
                        }
                    }
                    // No else: fallback to whatever mapping was present
                }
            }
        }
    }

    private float[] toFloatArray(JsonArray arr) {
        float[] out = new float[3];
        for (int i = 0; i < 3; ++i) out[i] = arr.get(i).getAsFloat();
        return out;
    }
    private JsonArray floatArrayToJson(float[] arr) {
        JsonArray json = new JsonArray();
        for (float v : arr) json.add(v);
        return json;
    }
    private JsonObject deepCopy(JsonObject model) {
        return JsonParser.parseString(model.toString()).getAsJsonObject();
    }
    private JsonElement deepCopy(JsonElement model) {
        return JsonParser.parseString(model.toString());
    }

    @Override
    public String getName() {
        return "Staff Block Models (Discovery Phase)";
    }
}
