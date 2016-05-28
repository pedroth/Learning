package windowThreeDim;

import java.awt.Color;

public class ZbufferShader extends ZBufferPrespective {

	public ZbufferShader() {
		super();
	}

	public Color getFragmentColor(Element e, int x, int y) {
//		float c = (float) Math.max(1 - z/10.0f,0);
//		float c = (float) Math.exp(-0.25 * z) + 0.0f;
//		c = (c > 1) ? 1.0f : c;
		 float c = (float) (Math.sin(10*z) * Math.sin(10*z));
		return new Color(c, c, c);
	}
}
