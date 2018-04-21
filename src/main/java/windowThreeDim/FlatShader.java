package windowThreeDim;

import algebra.TriVector;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class FlatShader extends ZBufferPerspective {
    protected Color shade;
    protected double shininess;
    protected double ambientLightParameter;
    protected double powerDecay;
    private List<TriVector> lightPoints;

    public FlatShader() {
        super();
        lightPoints = new LinkedList<TriVector>();
        shininess = 5.0;
        ambientLightParameter = 0.25;
        powerDecay = 0.0;
    }

    private static double pow(final double a, final double b) {
        double log = b * Math.log(a);
        return Math.exp(log);
    }

    public double getAmbientLightParameter() {
        return ambientLightParameter;
    }

    /**
     * @param ambientLightParameter a value between 0 and 1;
     */
    public void setAmbientLightParameter(double ambientLightParameter) {
        this.ambientLightParameter = ambientLightParameter;
    }

    public double getShininess() {
        return shininess;
    }

    public void setShininess(double shininess) {
        this.shininess = shininess;
    }

    public void addLightPoint(TriVector x) {
        lightPoints.add(x);
    }

    /**
     * removes all lights previously added.
     */
    public void removeLights() {
        lightPoints.removeAll(lightPoints);
    }

    @Override
    public void paintTriangle(Triangle element) {
        int size = lightPoints.size();
        double acmDiff = 0;
        double acmSpec = 0;
        TriVector point = TriVector.sum(
                TriVector.sum(element.getNPoint(0), element.getNPoint(1)),
                element.getNPoint(2));
        point = TriVector.multConst(1 / 3, point);

        TriVector normal = Triangle.calcNormal(element.getPointsArray());
        /**
         * vector from surface to eye
         */
        TriVector v = TriVector.sub(this.getEyePos(), point);

        for (int i = 0; i < size; i++) {

            /**
             * vector from surface to lightPoint
             */
            TriVector dLight = TriVector.sub(lightPoints.get(i), point);

            TriVector h = TriVector.sum(v, dLight);

            h.normalize();
            double power = dLight.getLength();
            final double irrandiance = powerDecay == 0.0 ? 1 : 1 / (power * power);
            dLight.normalize();

            double dotDiff = irrandiance * Math.max(TriVector.vInnerProduct(normal, dLight), 0);
            double dotSpecular = irrandiance * pow(Math.max(TriVector.vInnerProduct(normal, h), 0), shininess);
            acmDiff += dotDiff;
            acmSpec += dotSpecular;
            acmSpec = Math.min(acmSpec, 1);
            acmDiff = Math.min(acmDiff, 1);
        }

        float[] colorRGB = element.getColor().getRGBColorComponents(null);

        float[] shinyWhite = new float[3];
        float[] ambient = new float[3];
        float[] color = new float[3];

        shinyWhite[0] = 1.0f;
        shinyWhite[1] = 1.0f;
        shinyWhite[2] = 1.0f;

        color[0] = colorRGB[0];
        color[1] = colorRGB[1];
        color[2] = colorRGB[2];

        color[0] *= acmDiff;
        color[1] *= acmDiff;
        color[2] *= acmDiff;

        shinyWhite[0] *= acmSpec;
        shinyWhite[1] *= acmSpec;
        shinyWhite[2] *= acmSpec;

        ambient[0] = colorRGB[0];
        ambient[1] = colorRGB[1];
        ambient[2] = colorRGB[2];

        ambient[0] *= ambientLightParameter;
        ambient[1] *= ambientLightParameter;
        ambient[2] *= ambientLightParameter;

        color[0] += shinyWhite[0];
        color[1] += shinyWhite[1];
        color[2] += shinyWhite[2];

        color[0] += ambient[0];
        color[1] += ambient[1];
        color[2] += ambient[2];

        color[0] = Math.min(color[0], 1.0f);
        color[1] = Math.min(color[1], 1.0f);
        color[2] = Math.min(color[2], 1.0f);

        color[0] = Math.max(color[0], 0.0f);
        color[1] = Math.max(color[1], 0.0f);
        color[2] = Math.max(color[2], 0.0f);


        shade = new Color(color[0], color[1], color[2]);
        super.paintTriangle(element);
    }

    public void boundColor(TriVector c) {
        c.setX(Math.min(c.getX(), 1));
        c.setY(Math.min(c.getY(), 1));
        c.setZ(Math.min(c.getZ(), 1));
    }

    public void changeNthLight(int n, TriVector lightPoint) {
        lightPoints.set(n, lightPoint);
    }

    public Color getFragmentColor(Element e, int x, int y) {
        return shade;
    }

    public void setPowerDecay(double powerDecay) {
        this.powerDecay = powerDecay;
    }
}
