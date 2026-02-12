package art.ameliah.fabric.autosprintfix.gui.util;

/**
 * Utility class for animations and easing functions.
 */
public class AnimationUtils {

    /**
     * Linear interpolation between two values.
     * 
     * @param start    Starting value
     * @param end      Ending value
     * @param progress Progress (0-1)
     * @return Interpolated value
     */
    public static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    /**
     * Ease-in-out cubic interpolation.
     * 
     * @param progress Progress (0-1)
     * @return Eased value
     */
    public static float easeInOutCubic(float progress) {
        if (progress < 0.5f) {
            return 4 * progress * progress * progress;
        } else {
            float f = (2 * progress - 2);
            return 0.5f * f * f * f + 1;
        }
    }

    /**
     * Ease-out cubic interpolation.
     * 
     * @param progress Progress (0-1)
     * @return Eased value
     */
    public static float easeOutCubic(float progress) {
        float f = progress - 1;
        return f * f * f + 1;
    }

    /**
     * Ease-in cubic interpolation.
     * 
     * @param progress Progress (0-1)
     * @return Eased value
     */
    public static float easeInCubic(float progress) {
        return progress * progress * progress;
    }

    /**
     * Bounce easing at the end.
     * 
     * @param progress Progress (0-1)
     * @return Eased value
     */
    public static float easeOutBounce(float progress) {
        if (progress < 1 / 2.75f) {
            return 7.5625f * progress * progress;
        } else if (progress < 2 / 2.75f) {
            progress -= 1.5f / 2.75f;
            return 7.5625f * progress * progress + 0.75f;
        } else if (progress < 2.5 / 2.75) {
            progress -= 2.25f / 2.75f;
            return 7.5625f * progress * progress + 0.9375f;
        } else {
            progress -= 2.625f / 2.75f;
            return 7.5625f * progress * progress + 0.984375f;
        }
    }

    /**
     * Elastic easing at the end.
     * 
     * @param progress Progress (0-1)
     * @return Eased value
     */
    public static float easeOutElastic(float progress) {
        if (progress == 0 || progress == 1) {
            return progress;
        }

        float p = 0.3f;
        float s = p / 4;

        return (float) (Math.pow(2, -10 * progress) * Math.sin((progress - s) * (2 * Math.PI) / p) + 1);
    }
}