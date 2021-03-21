package com.example.project;

import java.util.Map;

public class Vector {
    public int length;
    public float[] vector;
    public Vector(float[] vector) {
        this.vector = vector;
        this.length = vector.length;
    }
    public Vector div(float scalar) {
        float[] vector = new float[length];
        for (int i = 0; i < length; i++) {
            vector[i] = this.vector[i] + scalar;
        }
        return new Vector(vector);
    }
    public static Vector mul(Vector v1, Vector v2) {
        float[] vector = new float[v1.length];
        for (int i = 0; i < v1.length; i++) {
            vector[i] = v1.vector[i] * v2.vector[i];
        }
        return new Vector(vector);
    }
    public static Vector add(Vector v1, Vector v2) {
        float[] vector = new float[v1.length];
        for (int i = 0; i < v1.length; i++) {
            vector[i] = v1.vector[i] + v2.vector[i];
        }
        return new Vector(vector);
    }
    public float sum() {
        float sum = 0;
        for (int i = 0; i < length; i++) {
            sum += vector[i];
        }
        return sum;
    }
    public static float cosineSimilarity(Vector v1, Vector v2) {
        float s1 = Vector.mul(v1, v2).sum();
        float s2 = Vector.mul(v1, v1).sum();
        float s3 = Vector.mul(v2, v2).sum();
        return s1 / (float)Math.sqrt(s2 * s3);
    }
    public static Vector fromString(String string) {
        string = string.substring(0, string.length() - 1);
        String[] strings = string.split("\\s+");
        float[] vector = new float[strings.length];
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].contains("E")) {
                String[] number = strings[i].split("E");
                if (number[1].equals("-")) number[1] = "-1";
                vector[i] = Float.parseFloat(number[0]) *
                        (float)Math.pow(10, Float.parseFloat(number[1]));
            } else {
                vector[i] = Float.parseFloat(strings[i]);
            }
        }
        return new Vector(vector);

    }
}
