package com.genymobile.scrcpy.custom;

public class TouchMockUtils {

    public static void mockClick(int displayId,int x, int y) {

    }

    public static void mockDoubleClick(int displayId,int x, int y) {

    }

    public static void mockLongClick(int displayId,int x, int y) {

    }

    public static void mockDrag(int displayId,int startX, int startY, int endX, int endy, int durationMillis) {
        DragMockImpl helper = new DragMockImpl();
        helper.performParabolicDrag(displayId, startX, startY, endX, endy, durationMillis);
    }
}
