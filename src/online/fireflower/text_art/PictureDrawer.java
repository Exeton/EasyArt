package online.fireflower.text_art;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import org.apache.commons.lang.time.StopWatch;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

public class PictureDrawer implements CommandExecutor {

    private static final int buildOffset = 35;
    StopWatch stopWatch = new StopWatch();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        //Bukkit.getLogger().info("0");

        if (strings.length != 1 || !(commandSender instanceof Player))
            return false;

        //Bukkit.getLogger().info("1");

        try{
            URL url = new URL(strings[0]);
            BufferedImage image;




            //Bukkit.getLogger().info("12");
            try{
                //Bukkit.getLogger().info("34");
                image = ImageIO.read(url);
                image = resize(image);

                final BufferedImage finalImage = image;

                Bukkit.getLogger().info("Size: " + image.getWidth() + "x" + image.getHeight());
                stopWatch = new StopWatch();
                stopWatch.start();

                drawImage(finalImage, (Player)commandSender);//ToDo add async suppourt


            }catch (IOException e){
                commandSender.sendMessage("IO exception when connecting to " + strings[0]);
                return true;
            }

        }catch (MalformedURLException exception){
            commandSender.sendMessage(ChatColor.RED + "Invalid URL");
            return true;
        }

        commandSender.sendMessage(ChatColor.GOLD + "Finished drawing!");
        return true;
    }

    //https://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
    public BufferedImage resize(BufferedImage input){

        //168

        double SCALE = (double)100 / input.getWidth();

        if (SCALE >= 1)
            SCALE = 1;

        //double SCALE = 1;
        //double SCALE = 0.05;

        BufferedImage bi = new BufferedImage((int)(SCALE * input.getWidth()),
                (int)(SCALE * input.getHeight()),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D grph = (Graphics2D) bi.getGraphics();
        grph.scale(SCALE, SCALE);

        grph.drawImage(input, 0, 0, null);
        grph.dispose();

        return bi;
    }

    //Calculates what direction the player is facing, and the two perpendicular vectors, to draw the text art oriented to the players direction
    private void drawImage(BufferedImage image, Player player){

        Vector playerFacing = getVectorFromLargestDirection(player.getLocation().getDirection());
        Vector loc = player.getLocation().toVector();
        loc.add(playerFacing.clone().multiply(buildOffset));

        Vector xItterator = calculateLeftToRightVector(player, playerFacing);
        Vector yItterator = xItterator.clone().crossProduct(playerFacing.clone());

        //Center the text
        loc.subtract(xItterator.clone().multiply(image.getWidth() / 2));
        drawArt(loc, player.getWorld(), image, xItterator, yItterator);
    }

    private Vector calculateLeftToRightVector(Player player, Vector playerFacing){

        Location rotatedLoc = player.getLocation().clone();
        rotatedLoc.setYaw(rotatedLoc.getYaw() + 90);//We add 90 because we want this loc to be facing right of the player

        if (playerFacing.getBlockY() != 0){//If we're looking downwards, we won't want the rotated loc to be <0, -1, 0>
            rotatedLoc.setPitch(0);
        }
        return getVectorFromLargestDirection(rotatedLoc.getDirection());
    }

    private void drawArt(Vector loc, World world, BufferedImage image, Vector xItterator, Vector yItterator){


        Bukkit.getLogger().info("Drawing Art");
        int imageHeight = image.getHeight();

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), imageHeight * image.getWidth() + 5);

        for (int i = 0; i < image.getWidth(); i++){
            for (int j = 0; j < imageHeight; j++){
                int mat = closestBlockType(image.getRGB(i, imageHeight - j - 1));

                Vector offSet = xItterator.clone().multiply(i).add(yItterator.clone().multiply(j));
                //Location worldLoc = new Location(world, loc.getBlockX() + offSet.getBlockX(), loc.getBlockY() + offSet.getBlockY(), loc.getBlockZ() + offSet.getBlockZ());



                com.sk89q.worldedit.Vector location = new com.sk89q.worldedit.Vector(loc.getBlockX() + offSet.getBlockX(), loc.getBlockY() + offSet.getBlockY(), loc.getBlockZ() + offSet.getBlockZ());

                try{
                    editSession.setBlock(location, new BaseBlock(Material.STAINED_CLAY.getId(), 15 - mat));
                }catch (Exception e){
                    System.out.println(e.toString());
                    return;
                }
            }
        }


        Bukkit.getLogger().info( "" + stopWatch.getTime());

        Bukkit.getLogger().info("Commiting");
        editSession.commit();
    }

    private static int[][] generateARGB(BufferedImage image){
        int[][] result = new int[image.getWidth()][image.getHeight()];
        for (int i = 0; i < result.length; i++)
            for (int j = 0; j < result[0].length; j++)
                result[i][j] = image.getRGB(i, j);

        return result;
    }

    private Vector getVectorFromLargestDirection(Vector facing){

        double x = Math.abs(facing.getX());
        double y = Math.abs(facing.getY());
        double z = Math.abs(facing.getZ());

        Vector result = null;

        if (largest(x, y, z))
            result = new Vector(facing.getX(), 0,0);
        if (largest(y, x, z))
            result = new Vector(0, facing.getY(),0);
        if (largest(z, x, y))
            result = new Vector(0, 0,facing.getZ());

        return result.normalize();
    }

    private boolean largest(double expectedLargest, double otherA, double otherB){

        return (expectedLargest > otherA) && (expectedLargest > otherB);

    }


    public static Set<Integer> rgbBlockValues;
    public static HashMap<Integer, Byte> BlockValuesAndDamages = new HashMap<>();

    public static byte closestBlockType(int rgbInput){

        //From https://stackoverflow.com/questions/4801366/convert-rgb-values-to-integer/4801397
        int r = (rgbInput >> 16) & 0xFF;
        int g = (rgbInput >> 8) & 0xFF;
        int b = rgbInput & 0xFF;

        int lowestDif = 1000000;
        int closestRgb = 0;

        for (int rgbMaterialValue : rgbBlockValues){

            //From https://stackoverflow.com/questions/4801366/convert-rgb-values-to-integer/4801397
            int red = (rgbMaterialValue >> 16) & 0xFF;
            int green = (rgbMaterialValue >> 8) & 0xFF;
            int blue = rgbMaterialValue & 0xFF;


            int dr = red - r;
            int db = blue - b;
            int dg = green - g;

            int diff = Math.abs(dr) + Math.abs(db) + Math.abs(dg);
            //int diff = dr * dr + db * db + dg * dg;

            if (diff < lowestDif){
                lowestDif = diff;
                closestRgb = rgbMaterialValue;
            }
        }
        return BlockValuesAndDamages.get(closestRgb);
    }
}
