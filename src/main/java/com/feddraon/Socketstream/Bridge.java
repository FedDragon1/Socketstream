package com.feddraon.Socketstream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.*;


public class Bridge {
    private static final int N_THREADS = 8;
    public static final int N_FILES = 3;
    public static final ExecutorService POOL = Executors.newFixedThreadPool(N_THREADS);
    private final Logger logger = LoggerFactory.getLogger(Bridge.class);
    private final String functionPath;
    private VideoCapture capture;
    private final Mat image = new Mat();
    private final Size size = new Size(132, 74);
    private int fileCounter = 0;
    private String rtmpURI;

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Bridge bridge = new Bridge("C:\\Users\\fedel\\AppData\\Local\\Packages\\Microsoft.MinecraftUWP_8wekyb3d8bbwe\\LocalState\\games\\com.mojang\\minecraftWorlds\\MSX5YkSXAAA=\\behavior_packs\\PyStream\\functions",
                "rtmp://127.0.0.1:1935/live/test001");

        long start = System.nanoTime();

        Future<String> fut = bridge.produceNextFrame();

        //Thread.sleep(100);

        Future<String> fut2 = bridge.produceNextFrame();

        fut.get();

        //Thread.sleep(100);

        fut2.get();


        long end = System.nanoTime();

        System.out.println((end-start) / 1_000_000_000.0 / 2);


        POOL.shutdown();
    }

    public Bridge(String functionPath, String rtmpURI) throws FileNotFoundException {

        this.rtmpURI = rtmpURI;

        this.capture = new VideoCapture(this.rtmpURI);

        this.functionPath = functionPath;
    }

    public void idle() {
        this.capture.read(image);
    }

    public Future<String> produceNextFrame() {
        this.capture.read(image);

        if (!image.size().equals(size)) {
            logger.warn("Resizing image, slow render speed expected.");
            try {
                Imgproc.resize(image, image, size);
            } catch (Exception e) {
                this.capture = new VideoCapture(this.rtmpURI);
                return produceNextFrame();
            }
        }

        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGB);

        Converter converter = new Converter(fileCounter, image.clone());
        fileCounter++;
        fileCounter %= N_FILES;
        return POOL.submit(converter);
    }

    public void setRtmpURI(String rtmpURI) {
        this.rtmpURI = rtmpURI;
        this.capture = new VideoCapture(this.rtmpURI);
    }

    class Converter implements Callable<String> {
        private final int name;
        private final Mat image;
        private final byte[] rgb = new byte[3];

        public Converter(int name, Mat image) {
            this.name = name;
            this.image = image;
        }

        @Override
        public String call() throws IOException {
            PrintStream stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(functionPath + "\\buff_" + name + ".mcfunction")), true);

            for (int y = 0; y < 74; y++) {
                for (int x = 0; x < 132; x++) {
                    image.get(y, x, rgb);
                    String command = "setblock 0 " + (33 - y) + " " + x + " " + ColorToBlock.closestMatch(rgb[0], rgb[1], rgb[2]);;
                    stream.println(command);
                }
            }
            return "/function buff_" + this.name;
        }
    }

}


class ColorToBlock {
    private ColorToBlock() {}

    public static final LinkedHashMap<Integer[], String> MAP = new LinkedHashMap<>() {
        {
            put(new Integer[]{103, 97, 87}, "log2 0");
            put(new Integer[]{151, 89, 55}, "log2 4");
            put(new Integer[]{168, 90, 50}, "planks 4");
            put(new Integer[]{136, 136, 137}, "stone 5");
            put(new Integer[]{85, 85, 85}, "bedrock 0");
            put(new Integer[]{217, 215, 210}, "log 2");
            put(new Integer[]{193, 179, 135}, "log 6");
            put(new Integer[]{192, 175, 121}, "planks 2");
            put(new Integer[]{8, 10, 15}, "concrete 15");
            put(new Integer[]{25, 27, 32}, "concrete_powder 15");
            put(new Integer[]{68, 30, 32}, "black_glazed_terracotta 4");
            put(new Integer[]{37, 23, 16}, "stained_hardened_clay 15");
            put(new Integer[]{21, 21, 26}, "wool 15");
            put(new Integer[]{45, 47, 143}, "concrete 11");
            put(new Integer[]{70, 73, 167}, "concrete_powder 11");
            put(new Integer[]{47, 65, 139}, "blue_glazed_terracotta 4");
            put(new Integer[]{74, 60, 91}, "stained_hardened_clay 11");
            put(new Integer[]{53, 57, 157}, "wool 11");
            put(new Integer[]{229, 226, 208}, "bone_block 0");
            put(new Integer[]{210, 206, 179}, "bone_block 4");
            put(new Integer[]{117, 95, 60}, "bookshelf 0");
            put(new Integer[]{151, 98, 83}, "brick_block 0");
            put(new Integer[]{96, 60, 32}, "concrete 12");
            put(new Integer[]{126, 85, 54}, "concrete_powder 12");
            put(new Integer[]{120, 106, 86}, "brown_glazed_terracotta 4");
            put(new Integer[]{77, 51, 36}, "stained_hardened_clay 12");
            put(new Integer[]{114, 72, 41}, "wool 12");
            put(new Integer[]{232, 227, 218}, "quartz_block 1");
            put(new Integer[]{232, 227, 217}, "quartz_block 5");
            put(new Integer[]{183, 97, 28}, "red_sandstone 1");
            put(new Integer[]{216, 203, 155}, "sandstone 1");
            put(new Integer[]{120, 119, 120}, "stonebrick 3");
            put(new Integer[]{161, 166, 179}, "clay 0");
            put(new Integer[]{16, 16, 16}, "coal_block 0");
            put(new Integer[]{116, 116, 116}, "coal_ore 0");
            put(new Integer[]{119, 86, 59}, "dirt 1");
            put(new Integer[]{128, 127, 128}, "cobblestone 0");
            put(new Integer[]{118, 118, 118}, "stonebrick 2");
            put(new Integer[]{129, 106, 70}, "crafting_table 0");
            put(new Integer[]{189, 102, 32}, "red_sandstone 2");
            put(new Integer[]{218, 206, 160}, "sandstone 2");
            put(new Integer[]{21, 119, 136}, "concrete 9");
            put(new Integer[]{37, 148, 157}, "concrete_powder 9");
            put(new Integer[]{52, 119, 125}, "cyan_glazed_terracotta 4");
            put(new Integer[]{87, 91, 91}, "stained_hardened_clay 9");
            put(new Integer[]{21, 138, 145}, "wool 9");
            put(new Integer[]{60, 47, 26}, "log2 1");
            put(new Integer[]{65, 43, 21}, "log2 5");
            put(new Integer[]{67, 43, 20}, "planks 5");
            put(new Integer[]{98, 237, 228}, "diamond_block 0");
            put(new Integer[]{125, 143, 141}, "diamond_ore 0");
            put(new Integer[]{189, 188, 189}, "stone 3");
            put(new Integer[]{134, 96, 67}, "dirt 0");
            put(new Integer[]{42, 203, 88}, "emerald_block 0");
            put(new Integer[]{117, 137, 124}, "emerald_ore 0");
            put(new Integer[]{220, 223, 158}, "end_stone 0");
            put(new Integer[]{218, 224, 162}, "end_bricks 0");
            put(new Integer[]{246, 208, 62}, "gold_block 0");
            put(new Integer[]{144, 140, 125}, "gold_ore 0");
            put(new Integer[]{149, 103, 86}, "stone 1");
            put(new Integer[]{132, 127, 127}, "gravel 0");
            put(new Integer[]{55, 58, 62}, "concrete 7");
            put(new Integer[]{77, 81, 85}, "concrete_powder 7");
            put(new Integer[]{83, 90, 94}, "gray_glazed_terracotta 4");
            put(new Integer[]{58, 42, 36}, "stained_hardened_clay 7");
            put(new Integer[]{63, 68, 72}, "wool 7");
            put(new Integer[]{73, 91, 36}, "concrete 13");
            put(new Integer[]{97, 119, 45}, "concrete_powder 13");
            put(new Integer[]{117, 142, 67}, "green_glazed_terracotta 4");
            put(new Integer[]{76, 83, 42}, "stained_hardened_clay 13");
            put(new Integer[]{85, 110, 28}, "wool 13");
            put(new Integer[]{166, 136, 38}, "hay_block 0");
            put(new Integer[]{166, 139, 12}, "hay_block 4");
            put(new Integer[]{220, 220, 220}, "iron_block 0");
            put(new Integer[]{136, 131, 127}, "iron_ore 0");
            put(new Integer[]{4, 4, 4}, "nether_brick 0");
            put(new Integer[]{85, 68, 25}, "log 3");
            put(new Integer[]{150, 109, 71}, "log 7");
            put(new Integer[]{160, 115, 81}, "planks 3");
            put(new Integer[]{31, 67, 140}, "lapis_block 0");
            put(new Integer[]{99, 111, 133}, "lapis_ore 0");
            put(new Integer[]{36, 137, 199}, "concrete 3");
            put(new Integer[]{74, 181, 213}, "concrete_powder 3");
            put(new Integer[]{95, 165, 209}, "light_blue_glazed_terracotta 4");
            put(new Integer[]{113, 109, 138}, "stained_hardened_clay 3");
            put(new Integer[]{58, 175, 217}, "wool 3");
            put(new Integer[]{125, 125, 115}, "concrete 8");
            put(new Integer[]{155, 155, 148}, "concrete_powder 8");
            put(new Integer[]{144, 166, 168}, "silver_glazed_terracotta 4");
            put(new Integer[]{135, 107, 98}, "stained_hardened_clay 4");
            put(new Integer[]{142, 142, 135}, "wool 4");
            put(new Integer[]{94, 169, 24}, "concrete 5");
            put(new Integer[]{125, 189, 42}, "concrete_powder 5");
            put(new Integer[]{163, 198, 55}, "lime_glazed_terracotta 4");
            put(new Integer[]{104, 118, 53}, "stained_hardened_clay 5");
            put(new Integer[]{112, 185, 26}, "wool 5");
            put(new Integer[]{169, 48, 159}, "concrete 2");
            put(new Integer[]{193, 84, 185}, "concrete_powder 2");
            put(new Integer[]{208, 100, 192}, "magenta_glazed_terracotta 4");
            put(new Integer[]{150, 88, 109}, "stained_hardened_clay 2");
            put(new Integer[]{190, 69, 180}, "wool 2");
            put(new Integer[]{114, 146, 30}, "melon_block 0");
            put(new Integer[]{110, 118, 95}, "mossy_cobblestone 0");
            put(new Integer[]{115, 121, 105}, "stonebrick 1");
            put(new Integer[]{98, 38, 38}, "netherrack 0");
            put(new Integer[]{118, 66, 62}, "quartz_ore 0");
            put(new Integer[]{115, 3, 3}, "nether_wart_block 0");
            put(new Integer[]{89, 59, 41}, "noteblock 0");
            put(new Integer[]{109, 85, 51}, "log 0");
            put(new Integer[]{151, 122, 73}, "log 4");
            put(new Integer[]{162, 131, 79}, "planks 0");
            put(new Integer[]{15, 11, 25}, "obsidian 0");
            put(new Integer[]{224, 97, 1}, "concrete 1");
            put(new Integer[]{227, 132, 32}, "concrete_powder 1");
            put(new Integer[]{155, 147, 92}, "orange_glazed_terracotta 4");
            put(new Integer[]{162, 84, 38}, "stained_hardened_clay 1");
            put(new Integer[]{241, 118, 20}, "wool 1");
            put(new Integer[]{214, 101, 143}, "concrete 6");
            put(new Integer[]{229, 153, 181}, "concrete_powder 6");
            put(new Integer[]{235, 155, 182}, "pink_glazed_terracotta 4");
            put(new Integer[]{162, 78, 79}, "stained_hardened_clay 6");
            put(new Integer[]{238, 141, 172}, "wool 6");
            put(new Integer[]{132, 135, 134}, "stone 6");
            put(new Integer[]{193, 193, 195}, "stone 4");
            put(new Integer[]{154, 107, 89}, "stone 2");
            put(new Integer[]{196, 115, 24}, "pumpkin 0");
            put(new Integer[]{100, 32, 156}, "concrete 10");
            put(new Integer[]{132, 56, 178}, "concrete_powder 10");
            put(new Integer[]{110, 48, 152}, "purple_glazed_terracotta 4");
            put(new Integer[]{118, 70, 86}, "stained_hardened_clay 10");
            put(new Integer[]{122, 42, 173}, "wool 10");
            put(new Integer[]{236, 230, 223}, "quartz_block 0");
            put(new Integer[]{236, 231, 224}, "quartz_block 2");
            put(new Integer[]{235, 230, 223}, "quartz_block 6");
            put(new Integer[]{148, 20, 3}, "redstone_block 0");
            put(new Integer[]{133, 108, 108}, "redstone_ore 0");
            put(new Integer[]{142, 33, 33}, "concrete 14");
            put(new Integer[]{168, 54, 51}, "concrete_powder 14");
            put(new Integer[]{182, 60, 53}, "red_glazed_terracotta 4");
            put(new Integer[]{191, 103, 33}, "sand 1");
            put(new Integer[]{187, 99, 29}, "red_sandstone 0");
            put(new Integer[]{181, 98, 31}, "red_sandstone 3");
            put(new Integer[]{143, 61, 47}, "stained_hardened_clay 14");
            put(new Integer[]{161, 39, 35}, "wool 14");
            put(new Integer[]{219, 207, 163}, "sand 0");
            put(new Integer[]{216, 203, 156}, "sandstone 0");
            put(new Integer[]{224, 214, 170}, "sandstone 3");
            put(new Integer[]{159, 159, 159}, "smooth_stone 0");
            put(new Integer[]{249, 254, 254}, "snow 0");
            put(new Integer[]{81, 62, 51}, "soul_sand 0");
            put(new Integer[]{196, 192, 75}, "sponge 0");
            put(new Integer[]{59, 38, 17}, "log 1");
            put(new Integer[]{109, 80, 47}, "log 5");
            put(new Integer[]{115, 85, 49}, "planks 1");
            put(new Integer[]{126, 126, 126}, "stone 0");
            put(new Integer[]{122, 122, 122}, "stonebrick 0");
            put(new Integer[]{152, 94, 68}, "hardened_clay 0");
            put(new Integer[]{182, 88, 84}, "tnt 0");
            put(new Integer[]{207, 213, 214}, "concrete 0");
            put(new Integer[]{226, 227, 228}, "concrete_powder 0");
            put(new Integer[]{188, 212, 203}, "white_glazed_terracotta 4");
            put(new Integer[]{210, 178, 161}, "stained_hardened_clay 4");
            put(new Integer[]{234, 236, 237}, "wool 4");
            put(new Integer[]{241, 175, 21}, "concrete 4");
            put(new Integer[]{233, 199, 55}, "concrete_powder 4");
            put(new Integer[]{234, 192, 89}, "yellow_glazed_terracotta 4");
            put(new Integer[]{186, 133, 35}, "stained_hardened_clay 4");
            put(new Integer[]{249, 198, 40}, "wool 4");
        }
    };

    public static final List<String> MAP_VALUES = MAP.values().stream().toList();

    public static String closestMatch(int r, int g, int b) {
        List<Double> list = MAP.keySet().stream()
                .mapToDouble(color -> getDifference(color[0], color[1], color[2], r, g, b))
                .boxed().toList();
        int minIndex = list.indexOf(Collections.min(list));
        return MAP_VALUES.get(minIndex);
    }

    public static double getDifference(int r1, int g1, int b1, int r2, int g2, int b2) {
        r2 &= 0xff;
        g2 &= 0xff;
        b2 &= 0xff;
        return Math.sqrt(
                Math.pow(((r2 - r1) * 0.3), 2) +
                        Math.pow(((g2 - g1) * 0.59), 2) +
                        Math.pow(((b2 - b1) * 0.11), 2)
        );
    }
}

