package meteordevelopment.meteorclient.addons;

import meteordevelopment.meteorclient.LemonClient;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;

import java.util.ArrayList;
import java.util.List;

public class AddonManager {
    public static final List<MeteorAddon> ADDONS = new ArrayList<>();

    public static void init() {
        // Meteor pseudo addon
        {
            LemonClient.ADDON = new MeteorAddon() {

                @Override
                public String getPackage() {
                    return "meteordevelopment.meteorclient";
                }

                @Override
                public String getWebsite() {
                    return "https://meteorclient.com";
                }

                @Override
                public GithubRepo getRepo() {
                    return new GithubRepo("MeteorDevelopment", "lemon-client");
                }

                @Override
                public String getCommit() {
                    String commit = LemonClient.MOD_META.getCustomValue(LemonClient.MOD_ID + ":commit").getAsString();
                    return commit.isEmpty() ? null : commit;
                }
            };

            ModMetadata metadata = FabricLoader.getInstance().getModContainer(LemonClient.MOD_ID).get().getMetadata();

            LemonClient.ADDON.name = metadata.getName();
            LemonClient.ADDON.authors = new String[metadata.getAuthors().size()];
            if (metadata.containsCustomValue(LemonClient.MOD_ID + ":color")) {
                LemonClient.ADDON.color.parse(metadata.getCustomValue(LemonClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                LemonClient.ADDON.authors[i++] = author.getName();
            }
        }

        // Addons
        for (EntrypointContainer<MeteorAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("meteor", MeteorAddon.class)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            MeteorAddon addon = entrypoint.getEntrypoint();

            addon.name = metadata.getName();
            addon.authors = new String[metadata.getAuthors().size()];
            if (metadata.containsCustomValue(LemonClient.MOD_ID + ":color")) {
                addon.color.parse(metadata.getCustomValue(LemonClient.MOD_ID + ":color").getAsString());
            }

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                addon.authors[i++] = author.getName();
            }

            ADDONS.add(addon);
        }
    }
}
