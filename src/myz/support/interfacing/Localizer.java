/**
 * 
 */
package myz.support.interfacing;

import java.lang.reflect.Field;

import myz.utilities.NMSUtils;

import org.bukkit.entity.Player;

/**
 * @author Jordan
 * 
 */
public enum Localizer {

	// Variants have been merged for sake of simplicity.
	AFRIKAANS("Afrikaans", "af_ZA"), ARABIC("العربية", "ar_SA"), BULGARIAN("Българ�?ки", "bg_BG"), CATALAN("Català",
			"ca_ES"), CZECH("Čeština", "cs_CZ"), CYMRAEG("Cymraeg", "cy_GB"), DANISH("Dansk", "da_DK"), GERMAN("Deutsch", "de_DE"), GREEK(
			"Ελληνικά", "el_GR"), ENGLISH("English", "en_CA"), PIRATE_SPEAK("Pirate Speak", "en_PT"), ESPERANTO(
			"Esperanto", "eo_EO"), SPANISH("Espanol", "es_ES"), ESTONIAN("Eesti", "et_EE"), EUSKARA("Euskara", "eu_ES"), FINNISH("Suomi",
			"fi_FI"), TAGALOG("Tagalog", "fil_PH"), FRENCH("Francais", "fr_FR"), GAEILGE("Gaeilge", "ga_IE"), GALICIAN("Galego", "gl_ES"), HEBREW(
			"עברית", "he_IL"), CROATIAN("Hrvatski", "hr_HR"), HUNGARIAN("Magyar", "hu_HU"), ARMENIAN("Հայերեն", "hy_AM"), BAHASA_INDONESIA(
			"Bahasa Indonesia", "id_ID"), ICELANDIC("�?slenska", "is_IS"), ITALIAN("Italiano", "it_IT"), JAPANESE("日本語", "ja_JP"), GEORGIAN(
			"ქ�?რთული", "ka_GE"), KOREAN("한국어", "ko_KR"), KERNEWEK("Kernewek", "kw_GB"), LINGUA_LATINA("Lingua latina",
			"la_LA"), LETZEBUERGESCH("Lëtzebuergesch", "lb_LU"), LITHUANIAN("Lietuvių", "lt_LT"), LATVIAN("Latviešu", "lv_LV"), MALAY_NZ(
			"Bahasa Melayu", "mi_NZ"), MALAY_MY("Bahasa Melayu", "ms_MY"), MALTI("Malti", "mt_MT"), NORWEGIAN("Norsk", "nb_NO"), DUTCH(
			"Nederlands", "nl_NL"), OCCITAN("Occitan", "oc_FR"), PORTUGUESE_BR("Português", "pt_BR"), PORTUGUESE_PT("Português", "pt_PT"), QUENYA(
			"Quenya", "qya_AA"), ROMANIAN("Română", "ro_RO"), RUSSIAN("Ру�?�?кий", "ru_RU"), SLOVENIAN("Slovenš�?ina",
			"sl_SI"), SERBIAN("Срп�?ки", "sr_SP"), SWEDISH("Svenska", "sv_SE"), THAI("ภาษาไทย", "th_TH"), tlhIngan_Hol(
			"tlhIngan Hol", "tlh_AA"), TURKISH("Türkçe", "tr_TR"), UKRAINIAN("Україн�?ька", "uk_UA"), VIETNAMESE(
			"Tiếng Việt", "vi_VI"), SIMPLIFIED_CHINESE("简体中文", "zh_CN"), TRADITIONAL_CHINESE("�?體中文", "zh_TW"), POLISH(
			"Polski", "pl_PL"), DEFAULT("Default", "default");

	private static final String[] englishVariants = new String[] { "en_AU", "en_GB", "fa_IR", "hi_IN", "ky_KG", "sk_SK" };
	private static final String[] spanishVariants = new String[] { "es_AR", "es_MX", "es_UY", "es_VE" };
	private static final String[] frenchVariants = new String[] { "fr_CA" };
	private static final String[] norwegianVariants = new String[] { "no_NO", "nn_NO" };

	private String name;
	private String code;

	private static Field field;

	private Localizer(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public static Localizer getByCode(String code) {
		for (Localizer l : values())
			if (l.getCode().equalsIgnoreCase(code))
				return l;
		for (String s : spanishVariants) {
			if (s.equalsIgnoreCase(code)) { return Localizer.SPANISH; }
		}
		for (String s : frenchVariants) {
			if (s.equalsIgnoreCase(code)) { return Localizer.FRENCH; }
		}
		for (String s : norwegianVariants) {
			if (s.equalsIgnoreCase(code)) { return Localizer.NORWEGIAN; }
		}
		return Localizer.ENGLISH;
	}

	public static Localizer getLocale(Player inPlayer) {
		try {
			Object nms = NMSUtils.castToNMS(inPlayer);
			if (field == null) {
				field = nms.getClass().getDeclaredField("locale");
				field.setAccessible(true);
			}
			Localizer code = getByCode((String) field.get(nms));
			return code;
		} catch (Exception exc) {
			exc.printStackTrace();
			return getByCode("en_CA");
		}
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
