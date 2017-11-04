package uk.co.terminological.mappers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import uk.co.terminological.datatypes.EavMap;


public class ProxyMapWrapper {
	
	/** converts a package and filename to a reasonable java class name */
	public static String className(String packagename, String filename) {
		filename = filename.substring(0, filename.lastIndexOf('.'));
		String sb = toCamelCase(filename); 
		return packagename+"."+sb.substring(0, 1).toUpperCase()
				+ (sb.length()>1 ? sb.substring(1) : "");
	}
	
	private static String toCamelCase(String input) {
		if (input==null || input.isEmpty()) return "";
		input = input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
		input = input.replaceAll("[^a-z0-9_]", "_");
		Pattern patt = Pattern.compile("_([a-z0-9])");
		Matcher m = patt.matcher(input);
		StringBuffer sb = new StringBuffer(input.length());
		while (m.find()) {
			String text = m.group(1);
			text = text.toUpperCase();
			m.appendReplacement(sb, Matcher.quoteReplacement(text));
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	public static String methodName(String attributeName) {
		attributeName = toCamelCase(attributeName);
		if (attributeName.isEmpty()) return "get";
		if (attributeName.startsWith("get")) return attributeName;
		return "get"+attributeName.substring(0, 1).toUpperCase()
				+ (attributeName.length() > 1 ? attributeName.substring(1) : "");
	}
	
	public static String fieldName(String entityName) {
		entityName = toCamelCase(entityName);
		if (entityName.isEmpty()) return "_";
		return "_"+entityName.substring(0, 1).toLowerCase()
				+ (entityName.length() > 1 ? entityName.substring(1) : "");
	}
	
	public static <T extends Object> Stream<T> bind(EavMap<String,String,String> map, Class<T> type) {
		return map.streamEntities().map(eav -> (T) proxy(eav.getValue(),type));
	}
	
	public static <T extends Object> Stream<T> bind(Stream<List<String>> in, Class<T> type, Function<Method,Integer> mapper) {
		return in.map(sl -> proxy(sl,type,mapper));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T proxy(final Map<String,String> av, Class<T> type) {
		
		final Map<String,String> methodMap = new HashMap<>();
		av.entrySet().stream().forEach(kv -> methodMap.put(methodName(kv.getKey()), kv.getValue()));
		
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				String value = methodMap.get(method.getName());
				if (value != null) {
					return StringCaster.cast(method.getReturnType(), value);
				} else {
					if (method.getName().equals("toString")) return "proxy class of "+type.getCanonicalName();
					if (method.getName().equals("equals")) return false;
					if (method.getName().equals("hashcode")) return -1;
					throw new Exception("unknown method type: "+method.getName());
				}
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T proxy(final List<String> av, Class<T> type, Function<Method,Integer> methodMapper) {
		
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				try {
					Integer key = methodMapper.apply(method);
					return av.get(key);
				} catch (Exception e) {
					if (method.getName().equals("toString")) return "proxy class of "+type.getCanonicalName();
					if (method.getName().equals("equals")) return false;
					if (method.getName().equals("hashcode")) return -1;
					throw e;
				}
			}
		});
	}
}
