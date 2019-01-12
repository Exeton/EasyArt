package online.fireflower.text_art;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.awt.*;
import java.awt.image.BufferedImage;

public class TextArtCommand implements CommandExecutor {

    Font font = new Font("Serif", Font.PLAIN, 20);
    FontMetrics fontMetrics = Toolkit.getDefaultToolkit().getFontMetrics(font); //Font.getLineMetrics does not do what's needed.
    private static final int buildOffset = 35;

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (!(commandSender instanceof Player)){
            commandSender.sendMessage("You must be a player to use this command");
            return true;
        }

        String text = "";
        for (int i = 0; i < strings.length; i++)
            text += strings[i] + " ";

        boolean[][] characters = generateCharacater(text, font, fontMetrics.stringWidth(text), fontMetrics.getHeight(), fontMetrics.getAscent());
        drawText((Player)commandSender, characters);
        commandSender.sendMessage(ChatColor.GREEN + "Text drawn");
        return true;
    }

    //Calculates what direction the player is facing, and the two perpendicular vectors, to draw the text art oriented to the players direction
    private void drawText(Player player, boolean[][] characters){

        Vector playerFacing = getVectorFromLargestDirection(player.getLocation().getDirection());
        Vector loc = player.getLocation().toVector();
        loc.add(playerFacing.clone().multiply(buildOffset));

        Vector xItterator = calculateLeftToRightVector(player, playerFacing);
        Vector yItterator = xItterator.clone().crossProduct(playerFacing.clone());

        //Center the text
        loc.subtract(xItterator.clone().multiply(characters.length / 2));
        drawText(loc, player.getWorld(), characters, xItterator, yItterator);
    }

    private Vector calculateLeftToRightVector(Player player, Vector playerFacing){

        Location rotatedLoc = player.getLocation().clone();
        rotatedLoc.setYaw(rotatedLoc.getYaw() + 90);//We add 90 because we want this loc to be facing right of the player

        if (playerFacing.getBlockY() != 0){//If we're looking downwards, we won't want the rotated loc to be <0, -1, 0>
            rotatedLoc.setPitch(0);
        }
        return getVectorFromLargestDirection(rotatedLoc.getDirection());
    }

    private void drawText(Vector loc, World world, boolean[][] characters, Vector xItterator, Vector yItterator){

        for (int i = 0; i < characters.length; i++)
            for (int j = 0; j < characters[0].length; j++)
                if (characters[i][characters[0].length - j - 1]){

                    Vector offSet = xItterator.clone().multiply(i).add(yItterator.clone().multiply(j));
                    Location worldLoc = new Location(world, loc.getBlockX() + offSet.getBlockX(), loc.getBlockY() + offSet.getBlockY(), loc.getBlockZ() + offSet.getBlockZ());
                    world.getBlockAt(worldLoc).setType(Material.STONE);
                }
    }

    private static boolean[][] generateCharacater(String character, Font font, int width, int height, int assecent){

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setFont(font);
        graphics.setColor(Color.WHITE);
        graphics.drawString(character, 0, assecent);

        boolean[][] result = new boolean[width][height];
        for (int i = 0; i < width; i++)
            for (int j = 0; j < height; j++)
                result[i][j] = (image.getRGB(i, j) == -1);


        graphics.dispose();
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

}
