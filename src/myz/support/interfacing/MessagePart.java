/**
 * 
 */
package myz.support.interfacing;

import org.bukkit.ChatColor;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * @author mkremins
 * 
 */
public final class MessagePart {

	public ChatColor color = null;
	public ChatColor[] styles = null;
	public String clickActionName = null;
	public String clickActionData = null;
	public String hoverActionName = null;
	public String hoverActionData = null;
	final String text;

	public MessagePart(final String text) {
		this.text = text;
	}

	public JSONWriter writeJson(final JSONWriter json) throws JSONException {
		json.object().key("text").value(text);
		if (color != null)
			json.key("color").value(color.name().toLowerCase());
		if (styles != null)
			for (final ChatColor style : styles)
				json.key(style.name().toLowerCase()).value(true);
		if (clickActionName != null && clickActionData != null)
			json.key("clickEvent").object().key("action").value(clickActionName).key("value").value(clickActionData).endObject();
		if (hoverActionName != null && hoverActionData != null)
			json.key("hoverEvent").object().key("action").value(hoverActionName).key("value").value(hoverActionData).endObject();
		return json.endObject();
	}

}
