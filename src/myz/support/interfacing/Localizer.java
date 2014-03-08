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

	// English variants have been merged for sake of simplicity.
	AUSTRALIAN_ENGLISH("Australian English", "en_CA"/*"en_AU"*/), AFRIKAANS("Afrikaans", "af_ZA"), ARABIC("العربية", "ar_SA"), BULGARIAN(
			"Българ�?ки", "bg_BG"), CATALAN("Català", "ca_ES"), CZECH("Čeština", "cs_CZ"), CYMRAEG("Cymraeg", "cy_GB"), DANISH(
			"Dansk", "da_DK"), GERMAN("Deutsch", "de_DE"), GREEK("Ελληνικά", "el_GR"), CANADIAN_ENGLISH("Canadian English", "en_CA"), ENGLISH(
			"English", "en_CA"/*"en_GB"*/), PIRATE_SPEAK("Pirate Speak", "en_PT"), ESPERANTO("Esperanto", "eo_EO"), ARGENTINEAN_SPANISH(
			"Español Argentino", "es_AR"), SPANISH("Español", "es_ES"), MEXICO_SPANISH("Español México", "es_MX"), URUGUAY_SPANISH(
			"Español Uruguay", "es_UY"), VENEZUELA_SPANISH("Español Venezuela", "es_VE"), ESTONIAN("Eesti", "et_EE"), EUSKARA("Euskara",
			"eu_ES"), ENGLISH1("زبان انگلیسی", "en_CA"/*"fa_IR"*/), FINNISH("Suomi", "fi_FI"), TAGALOG("Tagalog", "fil_PH"), FRENCH_CA(
			"Français", "fr_CA"), FRENCH("Français", "fr_FR"), GAEILGE("Gaeilge", "ga_IE"), GALICIAN("Galego", "gl_ES"), HEBREW(
			"עברית", "he_IL"), ENGLISH2("अंग�?रेज़ी", "en_CA"/*"hi_IN"*/), CROATIAN("Hrvatski", "hr_HR"), HUNGARIAN(
			"Magyar", "hu_HU"), ARMENIAN("Հայերեն", "hy_AM"), BAHASA_INDONESIA("Bahasa Indonesia", "id_ID"), ICELANDIC(
			"�?slenska", "is_IS"), ITALIAN("Italiano", "it_IT"), JAPANESE("日本語", "ja_JP"), GEORGIAN("ქ�?რთული",
			"ka_GE"), KOREAN("한국어", "ko_KR"), KERNEWEK("Kernewek", "kw_GB"), ENGLISH3("अंग�?रेज़ी", "en_CA"/*"ky_KG"*/), LINGUA_LATINA(
			"Lingua latina", "la_LA"), LETZEBUERGESCH("Lëtzebuergesch", "lb_LU"), LITHUANIAN("Lietuvių", "lt_LT"), LATVIAN("Latviešu",
			"lv_LV"), MALAY_NZ("Bahasa Melayu", "mi_NZ"), MALAY_MY("Bahasa Melayu", "ms_MY"), MALTI("Malti", "mt_MT"), NORWEGIAN("Norsk",
			"nb_NO"), DUTCH("Nederlands", "nl_NL"), NORWEGIAN_NYNORSK("Norsk nynorsk", "nn_NO"), NORWEGIAN1("Norsk", "no_NO"), OCCITAN(
			"Occitan", "oc_FR"), PORTUGUESE_BR("Português", "pt_BR"), PORTUGUESE_PT("Português", "pt_PT"), QUENYA("Quenya", "qya_AA"), ROMANIAN(
			"Română", "ro_RO"), RUSSIAN("Ру�?�?кий", "ru_RU"), ENGLISH4("Angli�?tina", "en_CA"/*"sk_SK"*/), SLOVENIAN(
			"Slovenš�?ina", "sl_SI"), SERBIAN("Срп�?ки", "sr_SP"), SWEDISH("Svenska", "sv_SE"), THAI("ภาษาไทย",
			"th_TH"), tlhIngan_Hol("tlhIngan Hol", "tlh_AA"), TURKISH("Türkçe", "tr_TR"), UKRAINIAN("Україн�?ька", "uk_UA"), VIETNAMESE(
			"Tiếng Việt", "vi_VI"), SIMPLIFIED_CHINESE("简体中文", "zh_CN"), TRADITIONAL_CHINESE("�?體中文", "zh_TW"), POLISH(
			"Polski", "pl_PL"), DEFAULT("Default", "default");

	private String name;
	private String code;

	private Localizer(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	private static Field field;

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

	public static Localizer getByCode(String code) {
		for (Localizer l : values())
			if (l.getCode().equalsIgnoreCase(code))
				return l;
		return Localizer.ENGLISH;
	}
}
