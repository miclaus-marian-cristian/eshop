package com.mcm.product_catalog.util;

import java.util.Map;

public class ProductUtils {

	// method to generate a string of specific length
	public static String generateString(int length) {
		return new String(new char[length]).replace('\0', 'a');
	}

	// method to generate a map of attributes of specific size
	public static Map<String, String> generateAttributes(int size) {
		Map<String, String> attributes = new java.util.HashMap<>();
		for (int i = 1; i <= size; i++) {
			attributes.put("key" + i, "value");
		}
		return attributes;
	}

}
