package online.fireflower.text_art;

import org.bukkit.plugin.java.JavaPlugin;

public class TextArt extends JavaPlugin {

    @Override
    public void onEnable() {

        this.getCommand("write").setExecutor(new TextArtCommand());
    }

}
