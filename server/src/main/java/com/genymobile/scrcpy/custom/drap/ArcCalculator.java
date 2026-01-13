package com.genymobile.scrcpy.custom.drap;

import com.genymobile.scrcpy.custom.controll.Point;

import java.util.ArrayList;
import java.util.List;

public class ArcCalculator {
    
    // 计算两点之间的距离
    private static double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2));
    }

    public static Point calculatePointC(ArcParams params,boolean isArcCenterTop) {
        Point pointC = calculatePointC(params.start, params.end, params.arcHeight, true);
        return pointC;

    }
    // 计算C点坐标
    public static Point calculatePointC(Point start, Point end, double arcHeight,boolean isArcCenterTop) {
        // 计算中点
        double midX = (start.x + end.x) / 2.0;
        double midY = (start.y + end.y) / 2.0;
        
        // 计算直线方向向量
        double dx = end.x - start.x;
        double dy = end.y - start.y;

        // 直线长度
        double lineLength = Math.sqrt(dx * dx + dy * dy);

        // 单位方向向量
        double ux = dx / lineLength;
        double uy = dy / lineLength;

        // 法向量（垂直方向）- 选择左侧的法向量
        // 顺时针旋转90度得到指向左侧的法向量
        double nx = uy;   // 旋转矩阵: [0, -1; 1, 0] * [ux, uy]^T = [-uy, ux]
        double ny;
        if (isArcCenterTop) {
            ny = -ux;  // 但我们需要左侧，所以取反
            if (ux < 0) {
                ny = ux;
            }
        }else {
            ny = ux;  // 但我们需要左侧，所以取反
            if (ux < 0) {
                ny = -ux;
            }
        }

        // 3. 计算圆心 - 确保圆心在中点左侧
        // 圆心的 x 坐标应该小于中点的 x 坐标
        double cX = midX + arcHeight * nx;
        double cY = midY + arcHeight * ny;
        
//        // 计算直线的长度
//        double lineLength = distance(start, end);
//
//        // 计算直线的法向量（垂直方向）
//        // 法向量为 (-dy, dx) 或 (dy, -dx)，我们取上半部分所以取一个方向
//        // 对于"上半部分"，我们需要确定方向。这里假设从start到end，左侧为上半部分
//        double normalX = dx;
//        double normalY = dy;
//
//        // 单位化法向量
//        double normalLength = Math.sqrt(normalX * normalX + normalY * normalY);
//        if (normalLength > 0) {
//            normalX /= normalLength;
//            normalY /= normalLength;
//        }
//
//        // C点在中点的垂直方向上，距离为length0
//        double cX = midX + normalX * length0;
//        double cY = midY + normalY * length0;
        
        return new Point(cX, cY);
    }
    
    // 计算圆心
    private static Point calculateCircleCenter(Point p1, Point p2, Point p3) {
        double x1 = p1.x, y1 = p1.y;
        double x2 = p2.x, y2 = p2.y;
        double x3 = p3.x, y3 = p3.y;
        
        double A = x1 * (y2 - y3) - y1 * (x2 - x3) + x2 * y3 - x3 * y2;
        double B = (x1 * x1 + y1 * y1) * (y3 - y2) + 
                   (x2 * x2 + y2 * y2) * (y1 - y3) + 
                   (x3 * x3 + y3 * y3) * (y2 - y1);
        double C = (x1 * x1 + y1 * y1) * (x2 - x3) + 
                   (x2 * x2 + y2 * y2) * (x3 - x1) + 
                   (x3 * x3 + y3 * y3) * (x1 - x2);
        double D = (x1 * x1 + y1 * y1) * (x3 * y2 - x2 * y3) + 
                   (x2 * x2 + y2 * y2) * (x1 * y3 - x3 * y1) + 
                   (x3 * x3 + y3 * y3) * (x2 * y1 - x1 * y2);
        
        double centerX = -B / (2 * A);
        double centerY = -C / (2 * A);
        
        return new Point(centerX, centerY);
    }
    
    // 计算圆弧上的点
    public static List<Point> calculateArcPointsByEven(ArcParams arcParams, int numPoints) {
        List<Point> arcPoints = new ArrayList<>();

        Point pointC = arcParams.control;
        Point start = arcParams.start;
        Point end = arcParams.end;
        // 1. 计算C点
//        Point pointC = calculatePointC(start, end, arcHeight);
        
        // 2. 计算圆心
        Point center = calculateCircleCenter(start, pointC, end);
        
        // 3. 计算半径
        double radius = distance(center, start);
        
        // 4. 计算起始角度和结束角度
        double startAngle = Math.atan2(start.y - center.y, start.x - center.x);
        double endAngle = Math.atan2(end.y - center.y, end.x - center.x);
        
        // 确保角度正确（考虑方向）
        double angleC = Math.atan2(pointC.y - center.y, pointC.x - center.x);
        
        // 调整角度，确保经过C点
        if (startAngle > endAngle) {
            if (angleC > endAngle && angleC < startAngle) {
                // C点在中间，不需要调整
            } else {
                endAngle += 2 * Math.PI;
                if (angleC < startAngle) {
                    angleC += 2 * Math.PI;
                }
            }
        } else {
            if (angleC < startAngle || angleC > endAngle) {
                // 需要调整
                startAngle += 2 * Math.PI;
                if (angleC < endAngle) {
                    angleC += 2 * Math.PI;
                }
            }
        }
        
        // 5. 生成圆弧上的点
        for (int i = 0; i <= numPoints; i++) {
            double t = (double) i / numPoints;
            double angle = startAngle + (endAngle - startAngle) * t;
            
            double x = center.x + radius * Math.cos(angle);
            double y = center.y + radius * Math.sin(angle);
            
            arcPoints.add(new Point(x, y));
        }
        
        return arcPoints;
    }
    public static List<Point> calculateArcPointsBySet(ArcParams arcParams, int pointNum, double[] processPoints) {
        List<Point> arcPoints = new ArrayList<>();

        // 1. 计算C点
//        Point pointC = calculatePointC(start, end, arcHeight, true);
        Point pointC = arcParams.control;
        Point start = arcParams.start;
        Point end = arcParams.end;

        // 2. 计算圆心
        Point center = calculateCircleCenter(start, pointC, arcParams.end);

        // 3. 计算半径
        double radius = distance(center, start);

        // 4. 计算起始角度和结束角度
        double startAngle = Math.atan2(start.y - center.y, start.x - center.x);
        double endAngle = Math.atan2(end.y - center.y, end.x - center.x);

        // 确保角度正确（考虑方向）
        double angleC = Math.atan2(pointC.y - center.y, pointC.x - center.x);

        // 调整角度，确保经过C点
        if (startAngle > endAngle) {
            if (angleC > endAngle && angleC < startAngle) {
                // C点在中间，不需要调整
            } else {
                endAngle += 2 * Math.PI;
                if (angleC < startAngle) {
                    angleC += 2 * Math.PI;
                }
            }
        } else {
            if (angleC < startAngle || angleC > endAngle) {
                // 需要调整
                startAngle += 2 * Math.PI;
                if (angleC < endAngle) {
                    angleC += 2 * Math.PI;
                }
            }
        }
        arcPoints.add(new Point(start.x, start.y));
        int justNumPoints = Math.min(pointNum, processPoints==null?0:processPoints.length);
        // 5. 生成圆弧上的点
        for (int i = 1; i < justNumPoints; i++) {
            double adjustedT=processPoints[i];//默认是匀速的
            double angle = startAngle + (endAngle - startAngle) * adjustedT;
            double x = center.x + radius * Math.cos(angle);
            double y = center.y + radius * Math.sin(angle);
            arcPoints.add(new Point(x, y));
        }

        arcPoints.add(new Point(end.x, end.y));
        return arcPoints;
    }

    public static List<Point> calculateArcPointsAcceDec(ArcParams arcParams, int numPoints) {

        Point pointC = arcParams.control;
        Point start = arcParams.start;
        Point end = arcParams.end;

        List<Point> arcPoints = new ArrayList<>();
        // 2. 计算圆心
        Point center = calculateCircleCenter(start, pointC, end);

        // 3. 计算半径
        double radius = distance(center, start);

        // 4. 计算起始角度和结束角度
        double startAngle = Math.atan2(start.y - center.y, start.x - center.x);
        double endAngle = Math.atan2(end.y - center.y, end.x - center.x);

        // 确保角度正确（考虑方向）
        double angleC = Math.atan2(pointC.y - center.y, pointC.x - center.x);

        // 调整角度，确保经过C点
        if (startAngle > endAngle) {
            if (angleC > endAngle && angleC < startAngle) {
                // C点在中间，不需要调整
            } else {
                endAngle += 2 * Math.PI;
                if (angleC < startAngle) {
                    angleC += 2 * Math.PI;
                }
            }
        } else {
            if (angleC < startAngle || angleC > endAngle) {
                // 需要调整
                startAngle += 2 * Math.PI;
                if (angleC < endAngle) {
                    angleC += 2 * Math.PI;
                }
            }
        }
        arcPoints.add(new Point(start.x, start.y));
        // 5. 生成圆弧上的点
        for (int i = 1; i < numPoints; i++) {
            double t = (double) i / numPoints;
            double adjustedT=t;//默认是匀速的
            String densityType = "MIDDLE_SPARSE";
            switch (densityType) {
                case "MIDDLE_SPARSE":
                    // 中间稀疏，两端密集
                    adjustedT = 0.5 * (1 - Math.cos(Math.PI * t));
                    break;

                case "MIDDLE_DENSE":
                    // 中间密集，两端稀疏
                    adjustedT = Math.sin(Math.PI * t / 2);
                    break;

                case "SMOOTH":
                    // 平滑变化
                    adjustedT = t * t * (3 - 2 * t); // 平滑的S曲线
                    break;

                case "QUADRATIC":
                    // 二次变化
                    if (t < 0.5) {
                        adjustedT = 2 * t * t;
                    } else {
                        adjustedT = 1 - 2 * (1 - t) * (1 - t);
                    }
                    break;
                default:
                    adjustedT=t;//默认是匀速的
                    break;
            }
            double angle = startAngle + (endAngle - startAngle) * adjustedT;
            double x = center.x + radius * Math.cos(angle);
            double y = center.y + radius * Math.sin(angle);
//            double angle = startAngle + (endAngle - startAngle) * t;
//            double x = center.x + radius * Math.cos(angle);
//            double y = center.y + radius * Math.sin(angle);
            arcPoints.add(new Point(x, y));
        }
        double x = end.x ;
        double y = end.y;
        arcPoints.add(new Point(x, y));
        return arcPoints;
    }


    // 简化的Point类

}