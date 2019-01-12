package online.fireflower.text_art;

import org.bukkit.plugin.java.JavaPlugin;

public class TextArt extends JavaPlugin {

    //Common practice for Spigot plugins
    public static TextArt main;

    @Override
    public void onEnable() {

        main = this;

        this.getCommand("write").setExecutor(new TextArtCommand());
        this.getCommand("draw").setExecutor(new PictureDrawer());
        addRgbVals();
    }

    private static void addRgbVals(){

        //RGB values
        /*
            black.png:2430480
            red.png:9321518
            green.png:4936234

            brown.png:5059108
            blue.png:4864859
            purple.png:7750998

            cyan.png:5659227
            silver.png:8874593
            gray.png:3746340

            pink.png:10505550
            lime.png:6780212
            yellow.png:12157987

            light_blue.png:7367818
            magenta.png:9787244
            orange.png:10507045
            white.png:13742497
         */

        addMaterialAndColor(0, 2430480);
        addMaterialAndColor(1, 9321518);
        addMaterialAndColor(2, 4936234);

        addMaterialAndColor(3, 5059108);
        addMaterialAndColor(4, 4864859);
        addMaterialAndColor(5, 7750998);

        addMaterialAndColor(6, 5659227);
        addMaterialAndColor(7, 8874593);
        addMaterialAndColor(8, 3746340);

        addMaterialAndColor(9, 10505550);
        addMaterialAndColor(10, 6780212);
        addMaterialAndColor(11, 12157987);

        addMaterialAndColor(12, 7367818);
        addMaterialAndColor(13, 9787244);
        addMaterialAndColor(14, 10507045);
        addMaterialAndColor(15, 13742497);

        PictureDrawer.rgbBlockValues =  PictureDrawer.BlockValuesAndDamages.keySet();

    }

    //From https://stackoverflow.com/questions/4801366/convert-rgb-values-to-integer/4801397

    private static void addMaterialAndColor(int materialDamageValue, int rgb){
        PictureDrawer.BlockValuesAndDamages.put(rgb, (byte)materialDamageValue);
    }

}
