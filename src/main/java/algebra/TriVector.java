package algebra;

import java.lang.Math;


public class TriVector {
    public double x;
    public double y;
    public double z;

    public TriVector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public TriVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public void setX(double a) {
        this.x = a;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double a) {
        this.y = a;
    }

    public double getZ() {
        return this.z;
    }

    public void setZ(double a) {
        this.z = a;
    }

    public void normalize() {
        double norm = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        this.x /= norm;
        this.y /= norm;
        this.z /= norm;
    }

    public void Transformation(Matrix m) {
        double nx = m.selMatrix(1, 1) * this.x + m.selMatrix(1, 2) * this.y + m.selMatrix(1, 3) * this.z;
        double ny = m.selMatrix(2, 1) * this.x + m.selMatrix(2, 2) * this.y + m.selMatrix(2, 3) * this.z;
        double nz = m.selMatrix(3, 1) * this.x + m.selMatrix(3, 2) * this.y + m.selMatrix(3, 3) * this.z;
        this.x = nx;
        this.y = ny;
        this.z = nz;
    }

    public void setXYZMat(Matrix m) {
        this.x = m.selMatrix(1, 1);
        this.y = m.selMatrix(2, 1);
        this.z = m.selMatrix(3, 1);
    }

    public void sum(TriVector v1) {
        this.x += v1.x;
        this.y += v1.y;
        this.z += v1.z;
    }

    public TriVector copy() {
        TriVector res = new TriVector();
        res.x = this.x;
        res.y = this.y;
        res.z = this.z;
        return res;
    }

    public double getLength() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public void multConst(double x) {
        this.x *= x;
        this.y *= x;
        this.z *= x;
    }

    public double norm() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public void fillRandom(double min, double max) {
        this.x = Math.random() * (max - min) + min;
        this.y = Math.random() * (max - min) + min;
        this.z = Math.random() * (max - min) + min;
    }

    public static TriVector vectorProduct(TriVector v1, TriVector v2) {
        return new TriVector(
                v1.y * v2.z - v1.z * v2.y,
                v1.z * v2.x - v1.x * v2.z,
                v1.x * v2.y - v1.y * v2.x);
    }

    public static TriVector sum(TriVector u, TriVector v) {
        return new TriVector(u.x + v.x, u.y + v.y, u.z + v.z);
    }

    public static TriVector sub(TriVector u, TriVector v) {
        return new TriVector(u.x - v.x, u.y - v.y, u.z - v.z);
    }

    public static TriVector multConst(double x, TriVector v) {
        return new TriVector(v.x * x, v.y * x, v.z * x);
    }

    public static double dot(TriVector u, TriVector v) {
        return u.x * v.x + u.y * v.y + u.z * v.z;
    }

    public static TriVector Transformation(Matrix m, TriVector v) {
        return new TriVector(
                m.selMatrix(1, 1) * v.x + m.selMatrix(1, 2) * v.y + m.selMatrix(1, 3) * v.z,
                m.selMatrix(2, 1) * v.x + m.selMatrix(2, 2) * v.y + m.selMatrix(2, 3) * v.z,
                m.selMatrix(3, 1) * v.x + m.selMatrix(3, 2) * v.y + m.selMatrix(3, 3) * v.z
        );
    }
}
