package uk.co.terminological.mappers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.stream.Stream;

import uk.co.terminological.datatypes.EavMap;


public class ProxyMapWrapper {
	
	public static String methodName(String attributeName) {
		attributeName = attributeName.replaceAll("[^a-zA-Z0-9_]", "");
		if (attributeName.isEmpty()) return "get";
		if (attributeName.startsWith("get")) return attributeName;
		return "get"+attributeName.substring(0, 1).toUpperCase()
				+ (attributeName.length() > 1 ? attributeName.substring(1) : "");
	}
	
	public static String fieldName(String entityName) {
		entityName = entityName.replaceAll("[^a-zA-Z0-9_]", "");
		if (entityName.isEmpty()) return "_";
		return "_"+entityName.substring(0, 1).toLowerCase()
				+ (entityName.length() > 1 ? entityName.substring(1) : "");
	}
	
	public static <T extends Object> Stream<T> bind(EavMap<String,String,String> map, Class<T> type) {
		return map.streamEntities().map(eav -> (T) proxy(eav.getValue(),type));
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T proxy(final Map<String,String> av, Class<T> type) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				//This is probably slow
				String value = av.keySet().stream().filter(k -> methodName(k).equals(method.getName()))
					.findFirst().map(k -> av.get(k))
					.orElse(null);
				return StringCaster.cast(method.getReturnType(), value);
			}
		});
	}
}
