package cn.windor.ddtank.util;

public class ColorUtils {
    public static String add(String color1, String color2) {
        int r = Integer.parseInt(color1.substring(0, 2), 16) + Integer.parseInt(color2.substring(0, 2), 16);
        int g = Integer.parseInt(color1.substring(2, 4), 16) + Integer.parseInt(color2.substring(2, 4), 16);
        int b = Integer.parseInt(color1.substring(4, 6), 16) + Integer.parseInt(color2.substring(4, 6), 16);
        return getColor(r, g, b);
    }

    public static String sub(String color1, String color2) {
        int r = Integer.parseInt(color1.substring(0, 2), 16) - Integer.parseInt(color2.substring(0, 2), 16);
        int g = Integer.parseInt(color1.substring(2, 4), 16) - Integer.parseInt(color2.substring(2, 4), 16);
        int b = Integer.parseInt(color1.substring(4, 6), 16) - Integer.parseInt(color2.substring(4, 6), 16);
        return getColor(r, g, b);
    }

    public static boolean isSimColor(int r, int g, int b, String colorStr) {
        String[] colors = colorStr.split("\\|");
        for (String c : colors) {
            String[] split = c.split("-");
            String color = split[0];
            int offR = r - Integer.parseInt(color.substring(0, 2), 16);
            int offG = g - Integer.parseInt(color.substring(2, 4), 16);
            int offB = b - Integer.parseInt(color.substring(4, 6), 16);
            if (split.length == 2) {
                String colorOffset = split[1];
                if (Math.abs(offR) <= Integer.parseInt(colorOffset.substring(0, 2), 16)
                        && Math.abs(offG) <= Integer.parseInt(colorOffset.substring(2, 4), 16)
                        && Math.abs(offB) <= Integer.parseInt(colorOffset.substring(4, 6), 16)) {
                    return true;
                }
            } else {
                if (offR == 0 && offG == 0 && offB == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isSimColor(String color1, String color2) {
        if(color1 == null || color2 == null) {
            return false;
        }
        int r = Integer.parseInt(color1.substring(0, 2), 16);
        int g = Integer.parseInt(color1.substring(2, 4), 16);
        int b = Integer.parseInt(color1.substring(4, 6), 16);
        return isSimColor(r, g, b, color2);
    }

    private static String getColor(int r, int g, int b) {
        if (r > 255) {
            r = 255;
        }
        if (g > 255) {
            g = 255;
        }
        if (b > 255) {
            b = 255;
        }
        StringBuilder color = new StringBuilder();
        if (r < 16) {
            color.append(0);
        }
        color.append(Integer.toHexString(r));
        if (g < 16) {
            color.append(0);
        }
        color.append(Integer.toHexString(g));
        if (b < 16) {
            color.append(0);
        }
        color.append(Integer.toHexString(b));
        return color.toString();
    }


    public static void main(String[] args) {
        System.out.println(sub("9feeff", "303030"));
    }

}
