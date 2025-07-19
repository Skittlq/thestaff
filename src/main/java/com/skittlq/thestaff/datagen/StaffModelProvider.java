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
import net.minecraft.tags.TagKey;
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

            // 1. Load the empty staff model ONCE
            ResourceLocation emptyStaffModelLoc = ResourceLocation.fromNamespaceAndPath("thestaff", "models/item/purple_staff_empty.json");
            JsonObject staffTemplate = loadModel(emptyStaffModelLoc);

            if (staffTemplate == null) {
                System.out.println("[StaffModelProvider] Could not find empty staff model at: " + emptyStaffModelLoc);
                return CompletableFuture.completedFuture(null);
            }

            // You may want to extract socket position from the staff model!
            // For demo: hardcode (4, 19, 4) as origin
            float blockOffsetX = 4, blockOffsetY = 19, blockOffsetZ = 4;

            List<CompletableFuture<?>> tasks = new ArrayList<>();

            for (Item item : allowedItems) {
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

                // Try item model, then block model
                ResourceLocation[] locs = new ResourceLocation[]{
                        ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "models/block/" + itemId.getPath() + ".json"),
                        ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), "models/item/" + itemId.getPath() + ".json")
                };

                JsonObject blockModel = null;
                for (ResourceLocation loc : locs) {
                    blockModel = flattenModel(loc, new HashSet<>());
                    if (blockModel != null) break;
                }
                if (blockModel == null) {
                    System.out.println("[StaffModelProvider] No usable model found for " + itemId);
                    continue;
                }

                // --- Build new staff model ---
                JsonObject outModel = deepCopy(staffTemplate);

                // Merge textures
                JsonObject staffTextures = outModel.getAsJsonObject("textures");
                JsonObject blockTextures = blockModel.getAsJsonObject("textures");
                for (Map.Entry<String, JsonElement> entry : blockTextures.entrySet()) {
                    String key = entry.getKey();
                    // Avoid collision with "#1" or "0"
                    if (!staffTextures.has(key)) {
                        staffTextures.add(key, entry.getValue());
                    }
                }

                // Merge elements (offset block cubes to socket)
                JsonArray outElements = outModel.getAsJsonArray("elements");
                JsonArray blockElements = blockModel.has("elements") ? blockModel.getAsJsonArray("elements") : null;
                if (blockElements == null) {
                    System.out.println("[StaffModelProvider] Model " + itemId + " (flattened) has no 'elements' array, skipping.");
                    continue;
                }
                JsonArray offsetBlockElements = deepCopy(blockElements); // Defensive copy

                // For each element in the block, offset by socket position (from 0,0,0)
                scaleElementsToSocket(offsetBlockElements);
                unifyFaceTexturesSmart(blockTextures, offsetBlockElements);
                fixItemModelFaces(offsetBlockElements); // ensure faces are correct

                // Add to staff's elements
                for (JsonElement e : offsetBlockElements) outElements.add(e);

                // Save
                String modelName = "purple_staff_" + itemId.getPath();
                Path modelPath = output.getOutputFolder().resolve("assets/thestaff/models/item/" + modelName + ".json");
                DataProvider.saveStable(cache, outModel, modelPath);

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
        Optional<Resource> res = resourceManager.getResource(loc);
        if (res.isPresent()) {
            try (InputStream s = res.get().open()) {
                return JsonParser.parseReader(new InputStreamReader(s)).getAsJsonObject();
            } catch (IOException e) {
                System.out.println("[StaffModelProvider] IOException reading model " + loc + ": " + e.getMessage());
            }
        }
        return null;
    }

    // Scales and centers all elements to fit inside [4,19,4] to [12,27,12]
    private void scaleElementsToSocket(JsonArray elements) {
        // Socket bounding box
        float[] socketFrom = {4, 19, 4};
        float[] socketTo   = {12, 27, 12};

        // Find the bounding box of all elements
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

        float[] srcFrom = {minX, minY, minZ};
        float[] srcTo = {maxX, maxY, maxZ};
        float[] srcSize = {srcTo[0] - srcFrom[0], srcTo[1] - srcFrom[1], srcTo[2] - srcFrom[2]};
        float[] socketSize = {socketTo[0] - socketFrom[0], socketTo[1] - socketFrom[1], socketTo[2] - socketFrom[2]};

        // Handle degenerate cases (just in case)
        for (int i = 0; i < 3; ++i) if (srcSize[i] == 0) srcSize[i] = 1;

        float scale = Math.min(socketSize[0] / srcSize[0], Math.min(socketSize[1] / srcSize[1], socketSize[2] / srcSize[2]));

        // Centre in socket
        float[] offset = new float[] {
                socketFrom[0] - srcFrom[0] * scale + (socketSize[0] - srcSize[0] * scale) / 2f,
                socketFrom[1] - srcFrom[1] * scale + (socketSize[1] - srcSize[1] * scale) / 2f,
                socketFrom[2] - srcFrom[2] * scale + (socketSize[2] - srcSize[2] * scale) / 2f
        };

        for (JsonElement elem : elements) {
            JsonObject cube = elem.getAsJsonObject();
            float[] from = toFloatArray(cube.getAsJsonArray("from"));
            float[] to = toFloatArray(cube.getAsJsonArray("to"));
            for (int i = 0; i < 3; ++i) {
                from[i] = (from[i] - srcFrom[i]) * scale + socketFrom[i] + (socketSize[i] - srcSize[i] * scale) / 2f;
                to[i]   = (to[i] - srcFrom[i]) * scale + socketFrom[i] + (socketSize[i] - srcSize[i] * scale) / 2f;
            }
            cube.add("from", floatArrayToJson(from));
            cube.add("to", floatArrayToJson(to));
        }
    }


    private void unifyFaceTexturesSmart(JsonObject blockTextures, JsonArray elements) {
        boolean hasAll  = blockTextures.has("all");
        boolean hasSide = blockTextures.has("side");
        boolean hasFront = blockTextures.has("front");
        boolean hasBack  = blockTextures.has("back");
        boolean hasTop   = blockTextures.has("top");
        boolean hasBottom = blockTextures.has("bottom");

        for (JsonElement elem : elements) {
            JsonObject cube = elem.getAsJsonObject();
            if (!cube.has("faces")) continue;
            JsonObject faces = cube.getAsJsonObject("faces");

            for (String face : Arrays.asList("down", "up", "north", "south", "east", "west")) {
                if (faces.has(face)) {
                    JsonObject faceObj = faces.getAsJsonObject(face);

                    // Command/block with front, back, side
                    if (hasFront && hasBack && hasSide) {
                        switch (face) {
                            case "north":
                                faceObj.addProperty("texture", "#front");
                                break;
                            case "south":
                                faceObj.addProperty("texture", "#back");
                                break;
                            default:
                                faceObj.addProperty("texture", "#side");
                                break;
                        }
                    }
                    // Blocks with "all"
                    else if (hasAll) {
                        faceObj.addProperty("texture", "#all");
                    }
                    // Blocks with just "side", "top", "bottom"
                    else if (hasSide && hasTop && hasBottom) {
                        switch (face) {
                            case "up":
                                faceObj.addProperty("texture", "#top");
                                break;
                            case "down":
                                faceObj.addProperty("texture", "#bottom");
                                break;
                            default:
                                faceObj.addProperty("texture", "#side");
                                break;
                        }
                    }
                    // Default: keep the original mapping
                }
            }
        }
    }

    // Ensure all faces have "uv" and no "cullface"
    private void fixItemModelFaces(JsonArray elements) {
        List<String> facesList = Arrays.asList("down", "up", "north", "south", "east", "west");
        JsonArray defaultUV = floatArrayToJson(new float[] {0, 0, 16, 16});
        for (JsonElement elem : elements) {
            JsonObject cube = elem.getAsJsonObject();
            if (!cube.has("faces")) continue;
            JsonObject faces = cube.getAsJsonObject("faces");
            for (String face : facesList) {
                if (faces.has(face)) {
                    JsonObject faceObj = faces.getAsJsonObject(face);
                    if (!faceObj.has("uv")) {
                        faceObj.add("uv", deepCopy(defaultUV));
                    }
                    faceObj.remove("cullface"); // Remove if present
                }
            }
        }
    }

    private JsonObject flattenModel(ResourceLocation loc, Set<ResourceLocation> visited) {
        if (visited.contains(loc)) return null; // Infinite loop guard
        visited.add(loc);

        JsonObject model = loadModel(loc);
        if (model == null) return null;

        // If the model has a parent, recursively flatten
        if (model.has("parent")) {
            String parentPath = model.get("parent").getAsString();
            ResourceLocation parentLoc;
            if (parentPath.contains(":")) {
                // e.g. "minecraft:block/cube_all"
                String[] parts = parentPath.split(":", 2);
                String[] typeAndPath = parts[1].split("/", 2); // typeAndPath[0]=block/item, typeAndPath[1]=cube_all
                if (typeAndPath.length == 2) {
                    parentLoc = ResourceLocation.fromNamespaceAndPath(parts[0], "models/" + typeAndPath[0] + "/" + typeAndPath[1] + ".json");
                } else {
                    // Fallback for odd parent paths
                    parentLoc = ResourceLocation.fromNamespaceAndPath(parts[0], "models/" + typeAndPath[0] + ".json");
                }
            } else {
                // e.g. "block/cube_all"
                parentLoc = ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), "models/" + parentPath + ".json");
            }
            JsonObject parentModel = flattenModel(parentLoc, visited);
            if (parentModel != null) {
                // Inherit elements and textures if missing
                if (!model.has("elements") && parentModel.has("elements")) {
                    model.add("elements", parentModel.getAsJsonArray("elements"));
                }
                if (!model.has("textures") && parentModel.has("textures")) {
                    model.add("textures", parentModel.getAsJsonObject("textures"));
                }
            }
        }
        return model;
    }

    // Basic deep copy using Gson
    private JsonObject deepCopy(JsonObject model) {
        return JsonParser.parseString(model.toString()).getAsJsonObject();
    }
    private JsonArray deepCopy(JsonArray array) {
        return JsonParser.parseString(array.toString()).getAsJsonArray();
    }

    // Offset each element's "from" and "to" coordinates, and fix faces for item models
    private void offsetElements(JsonArray elements, float x, float y, float z) {
        for (JsonElement elem : elements) {
            JsonObject cube = elem.getAsJsonObject();
            float[] from = toFloatArray(cube.getAsJsonArray("from"));
            float[] to = toFloatArray(cube.getAsJsonArray("to"));
            for (int i = 0; i < 3; ++i) { from[i] += (i == 0 ? x : i == 1 ? y : z); }
            for (int i = 0; i < 3; ++i) { to[i] += (i == 0 ? x : i == 1 ? y : z); }
            cube.add("from", floatArrayToJson(from));
            cube.add("to", floatArrayToJson(to));
        }
        fixItemModelFaces(elements); // <-- Here’s the magic, sir
    }

    // Replace the 'from' and 'to' of ALL elements to fit the socket
    private void clampElementsToSocket(JsonArray elements) {
        // The only valid block is [4, 19, 4] -> [12, 27, 12]
        JsonArray from = floatArrayToJson(new float[] {4, 19, 4});
        JsonArray to   = floatArrayToJson(new float[] {12, 27, 12});
        for (JsonElement elem : elements) {
            JsonObject cube = elem.getAsJsonObject();
            cube.add("from", deepCopy(from));
            cube.add("to", deepCopy(to));
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

    @Override
    public String getName() {
        return "Staff Block Models (Discovery Phase)";
    }
}
