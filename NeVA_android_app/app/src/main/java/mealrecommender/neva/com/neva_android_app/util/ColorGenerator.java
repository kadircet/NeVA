package mealrecommender.neva.com.neva_android_app.util;

/**
 * Created by hakan on 2/27/18.
 */

public class ColorGenerator {
  public static int getColor(Object key) {
    int rgb = (key.hashCode()&0xffffff);
    if (rgb == 0xffffff) {
      rgb = 0;
    }
    int bgColor = (0xff<<24) | rgb;
    return bgColor;
  }
}
