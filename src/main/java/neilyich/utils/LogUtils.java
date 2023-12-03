package neilyich.utils;

public class LogUtils {
    public static volatile boolean debugEnabled = false;
    
    public static void println(Object o) {
        if (debugEnabled) {
            System.out.println(o);
        }
    }

    public static void println() {
        if (debugEnabled) {
            System.out.println();
        }
    }
}
