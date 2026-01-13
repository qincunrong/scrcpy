package com.genymobile.scrcpy.custom.drap;

import com.genymobile.scrcpy.custom.controll.Point;

public class ArcParams {
    public Point start;
    public Point end;
    public Point control;
    public int arcHeight;

    @Override
    public String toString() {
        return "{" +
                "start=" + start +
                ", end=" + end +
                ", control=" + control +
                ", arcHeight=" + arcHeight +
                '}';
    }

}