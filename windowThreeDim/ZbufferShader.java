package windowThreeDim;

import java.awt.Color;

public class ZbufferShader extends ZBufferPrespective {

	public ZbufferShader() {
		super();
	}

	public Color getFragmentColor(Element e, int x, int y) {
		//float c = (float) Math.exp(-0.25 * z);
		float c = (float) (Math.sin(10*z) * Math.sin(10*z));
		return new Color(c, c, c);
	}
}
