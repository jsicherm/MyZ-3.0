/**
 * 
 */
package myz.nmscode.compat;

/**
 * @author Jordan
 * 
 */
public enum Compat {

	v1_7_2("v1_7_R1"), v1_7_5("v1_7_R2");

	private final String s;

	private Compat(String s) {
		this.s = s;
	}

	public static Compat fromString(String text) {
		if (text != null)
			for (Compat c : Compat.values())
				if (text.equalsIgnoreCase(c.s))
					return c;
		return null;
	}
}
