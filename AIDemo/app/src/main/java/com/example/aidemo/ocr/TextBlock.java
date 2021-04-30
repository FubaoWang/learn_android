package com.example.aidemo.ocr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class TextBlock {
    public ArrayList<Point> boxPoint;
    public float boxScore;
    public int angleIndex;
    public float angleScore;
    public double angleTime;
    public String text;
    public float[] charScore;
    public double crnnTime;
    public double blockTime;

    public TextBlock(ArrayList<Point> boxPoint, float boxScore, int angleIndex, float angleScore, double angleTime, String text, float[] charScore, double crnnTime, double blockTime) {
        this.boxPoint = boxPoint;
        this.boxScore = boxScore;
        this.angleIndex = angleIndex;
        this.angleScore = angleScore;
        this.angleTime = angleTime;
        this.text = text;
        this.charScore = charScore;
        this.crnnTime = crnnTime;
        this.blockTime = blockTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextBlock)) return false;
        TextBlock textBlock = (TextBlock) o;
        return Float.compare(textBlock.boxScore, boxScore) == 0 &&
                angleIndex == textBlock.angleIndex &&
                Float.compare(textBlock.angleScore, angleScore) == 0 &&
                Double.compare(textBlock.angleTime, angleTime) == 0 &&
                Double.compare(textBlock.crnnTime, crnnTime) == 0 &&
                Double.compare(textBlock.blockTime, blockTime) == 0 &&
                boxPoint.equals(textBlock.boxPoint) &&
                text.equals(textBlock.text) &&
                Arrays.equals(charScore, textBlock.charScore);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(boxPoint, boxScore, angleIndex, angleScore, angleTime, text, crnnTime, blockTime);
        result = 31 * result + Arrays.hashCode(charScore);
        return result;
    }
}

class Point{
    public int x;
    public int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
