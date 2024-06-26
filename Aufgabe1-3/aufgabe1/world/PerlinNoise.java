package aufgabe1.world;

/**
 * Class for calculating the Perlin Noise filter
 * STYLE: procedural, because it is static and each method has a specific task that the program follows
 */
// GOOD: The class is well-organized with static methods for specific tasks, enhancing readability.
public class PerlinNoise {

    /**
     * Pre-generated table for gradients
     */
    static final int[] p = new int[512], permutation = {151, 160, 137, 91, 90, 15,
        131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23,
        190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33,
        88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166,
        77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244,
        102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196,
        135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123,
        5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42,
        223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
        129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228,
        251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
        49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254,
        138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
    };

    static {
        for (int i = 0; i < 256; i++) p[256 + i] = p[i] = permutation[i];
    }

    /**
     * Calculates the noise filter
     */
    // BAD: The method 'noise' lacks comments explaining the steps involved in Perlin noise calculation
    public static float noise(float x, float y) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;
        int gradientTopLeft = p[p[xi] + yi];
        int gradientTopRight = p[p[xi + 1] + yi];
        int gradientBottomLeft = p[p[xi] + yi + 1];
        int gradientBottomRight = p[p[xi + 1] + yi + 1];

        float xFraction = (float) (x - Math.floor(x));
        float yFraction = (float) (y - Math.floor(y));

        float d1 = dotProduct(gradientTopLeft, xFraction, yFraction);
        float d2 = dotProduct(gradientTopRight, xFraction - 1, yFraction);
        float d3 = dotProduct(gradientBottomLeft, xFraction, yFraction - 1);
        float d4 = dotProduct(gradientBottomRight, xFraction - 1, yFraction - 1);

        float u = fade(xFraction);
        float v = fade(yFraction);

        float x1Interpolated = interpolate(u, d1, d2);
        float x2Interpolated = interpolate(u, d3, d4);
        return interpolate(v, x1Interpolated, x2Interpolated);

    }

    /**
     * Calculates dot product of 2 values
     */
    private static float dotProduct(int hash, float x, float y) {
        return switch (hash & 3) {
            case 0 -> x + y;
            case 1 -> -x + y;
            case 2 -> x - y;
            case 3 -> -x - y;
            default -> 0;
        };
    }

    /**
     * Fades the given value t
     */
    private static float fade(float t) {
        return (float) (6 * Math.pow(t, 5) - 15 * Math.pow(t, 4) + 10 * Math.pow(t, 3));
    }

    /**
     * Interpolates 2 values
     */
    private static float interpolate(float amount, float left, float right) {
        return (right - left) * amount + left;
    }
}
