/**
 * 
 */
package uk.co.terminological.mappers;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import uk.co.terminological.datatypes.NoMatchException;


/**
 * @author RCHALLEN
 *
 */
public class StringCaster {
	
	String string;
	
	public static StringCaster get(String s) {
		StringCaster out = new StringCaster();
		out.string = s;
		return out;
	}
	
	public String asString() {
		return string;
	}
	
	public int asInt() {
		return cast(Integer.TYPE);
	}
	
	public boolean asBoolean() {
		return cast(Boolean.TYPE);
	}
	
	public File asFile() {
		if (string==null || string.isEmpty()) return null;
		return new File(string);
	}
	
	public UUID asUUID() {
		if (string==null || string.isEmpty()) return null;
		return UUID.fromString(string);
	}
	
	public Date asDate() throws ParseException {
		if (string==null || string.isEmpty()) return null;
		return DateFormatString.tryAll(string);
	}
	
	public <Y extends Enum<?>> Y asEnum(Class<Y> enumClazz) throws NoMatchException {
		if (string==null || string.isEmpty()) return null;
		return assignEnum(enumClazz, string);
	}
	
	public static <X extends Object> X cast(Class<X> fieldClazz, String string) {
		return StringCaster.get(string).cast(fieldClazz);
	}

	@SuppressWarnings("unchecked")
	public <X extends Object> X cast(Class<X> fieldClazz) {
		Object fieldObject = null;

		if (string == null) {
			return undefinedPrimitive(fieldClazz);
		}
		
		if (String.class.isAssignableFrom(fieldClazz)) {

			fieldObject = string;

		} else {

			if (fieldClazz.equals(Boolean.TYPE)
					|| Boolean.class.isAssignableFrom(fieldClazz)) {
				if (
						string.equalsIgnoreCase("true") ||
						string.equalsIgnoreCase("1") ||
						string.equalsIgnoreCase("yes") ||
						string.equalsIgnoreCase("y") ||
						string.equalsIgnoreCase("t") 
				) fieldObject = true;
				else fieldObject = false;
				//log.debug("Boolean text: "+string+" value: "+ fieldObject.toString());

			} else if (fieldClazz.equals(Short.TYPE)
					|| Short.class.isAssignableFrom(fieldClazz)) {
				fieldObject = Short.parseShort(string);

			} else if (fieldClazz.equals(Integer.TYPE)
					|| Integer.class.isAssignableFrom(fieldClazz)) {
				Matcher m = Pattern.compile("^([0-9]+)[^0-9]*.*$").matcher(string);
				if (m.matches()) fieldObject = Integer.parseInt(m.group(1));
				else throw new NumberFormatException(string+" is not an integer");

			} else if (fieldClazz.equals(Character.TYPE)
					|| Character.class.isAssignableFrom(fieldClazz)) {
				fieldObject = string.charAt(0);

			} else if (fieldClazz.equals(Byte.TYPE)
					|| Byte.class.isAssignableFrom(fieldClazz)) {
				fieldObject = string.charAt(0);

			} else if (fieldClazz.equals(Long.TYPE)
					|| Long.class.isAssignableFrom(fieldClazz)) {
				fieldObject = Long.parseLong(string);

			} else if (fieldClazz.equals(Double.TYPE)
					|| Double.class.isAssignableFrom(fieldClazz)) {
				fieldObject = Double.parseDouble(string);

			} else if (fieldClazz.equals(Float.TYPE)
					|| Float.class.isAssignableFrom(fieldClazz)) {
				fieldObject = Float.parseFloat(string);

			} else if (fieldClazz.equals(Void.TYPE) 
					|| Void.class.isAssignableFrom(fieldClazz)) {
				throw new RuntimeException("void type");

			} else if (UUID.class.isAssignableFrom(fieldClazz)) {
				return (X) asUUID();
			
			} else if (URI.class.isAssignableFrom(fieldClazz)) {
				return (X) asURI();
			
			} else if (File.class.isAssignableFrom(fieldClazz)) {
				return (X) asFile();
			
			} else if (Enum.class.isAssignableFrom(fieldClazz)) {
				try {
					return (X) asEnum((Class<Enum<?>>) fieldClazz);
				} catch (NoMatchException e) {
					throw new ClassCastException("Cannot map value "+string+" to "+fieldClazz.getCanonicalName());
				}
			
			} else if (Date.class.isAssignableFrom(fieldClazz)) {
				try {
					return (X) asDate();
				} catch (ParseException e) {
					throw new ClassCastException("Unparseable date string: "+string);
				}
			} else if (XMLGregorianCalendar.class.isAssignableFrom(fieldClazz)) {
				try {
					return (X) DatatypeFactory.newInstance().newXMLGregorianCalendar(string);
				} catch (DatatypeConfigurationException | IllegalArgumentException e) {
					throw new ClassCastException("Unparseable Xml date string: "+string);
				}
			} else if (URL.class.isAssignableFrom(fieldClazz)) {
				try {
					return (X) new URL(string);
				} catch (MalformedURLException e) {
					throw new ClassCastException("Unparseable url string: "+string);
				}
			
			} else {
				throw new ClassCastException("Unsupported type: "+fieldClazz.getName()+" for "+string);
			}
		}
		return (X) fieldObject;

	}
	
	public URI asURI() {
		if (string==null || string.isEmpty()) return null;
		return URI.create(string);
	}

	@SuppressWarnings("unchecked")
	private static <Y extends Object> Y undefinedPrimitive(Class<Y> fieldClazz) {
		Object fieldObject;
		if (fieldClazz.equals(Boolean.TYPE)) {
			fieldObject = false;
		} else if (fieldClazz.equals(Short.TYPE)) {
			fieldObject = 0;
		} else if (fieldClazz.equals(Integer.TYPE)) {
			fieldObject = 0;
		} else if (fieldClazz.equals(Character.TYPE)) {
			fieldObject = 0;
		} else if (fieldClazz.equals(Byte.TYPE)) {
			fieldObject = 0;
		} else if (fieldClazz.equals(Long.TYPE)) {
			fieldObject = 0L;
		} else if (fieldClazz.equals(Double.TYPE)) {
			fieldObject = 0D;
		} else if (fieldClazz.equals(Float.TYPE)) {
			fieldObject = 0.0;
		} else {
			fieldObject = null;
		}
		return (Y) fieldObject;
	}
	
	private static <T extends Enum<?>> T assignEnum(Class<T> enumClazz, Object o) throws NoMatchException {
		String prop = null;
		for (Field f: enumClazz.getFields()) {
			if (f.isEnumConstant()) {
				if (f.getName().equalsIgnoreCase(o.toString().trim())) {
					prop = f.getName();
				}
			}
		}
		if (prop != null) {
			for (T out:enumClazz.getEnumConstants()) {
				if (out.name().equals(prop)) return out;
			}
		}
		throw new NoMatchException("Unsupported value "+o.toString()+" for enumeration "+enumClazz.getCanonicalName());
	}
	
	public static Class<?> guessType(String s) {
		if (s == null || s.isEmpty()) return Object.class;
		if (s.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")) return UUID.class;
		if (s.matches("[-+]?\\d+")) return Integer.class;
		if (s.matches("[-+]?[0-9]*\\.[0-9]+")) return Float.class;
		if (s.toLowerCase().matches("t|f|y|n|true|false|yes|no")) return Boolean.class;
		try { 
			DateFormatString.tryAll(s);
			return Date.class;
		} catch (ParseException e) {
			//do nothing
		}
		return String.class;
	}
	
	
	public static Map<String,Class<?>> guessTypes(Map<String, String> types) {
		Map<String,Class<?>> out = new LinkedHashMap<>();
		for (Entry<String, String> type : types.entrySet()) {
			out.put(type.getKey(), StringCaster.guessType(type.getValue()));
		}
		return out;
	}

}
