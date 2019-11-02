package com.android.servicesample.stringee.common;


import com.android.servicesample.stringee.define.StringeeKeys;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 *
 * @author Stringee
 */
public class StringeeToken {

	public static String create(String userId) {
		Map<String, Object> headerClaims = new HashMap<String, Object>();
		headerClaims.put("typ", "JWT");
		headerClaims.put("cty", "stringee-api;v=1");
		headerClaims.put("alg", "HS256");
		Date exp = new Date((System.currentTimeMillis()) + StringeeKeys.STRINGEE_EXPIRE_INSECOND * 1000);

		byte[] keyBytes = StringeeKeys.STRINGEE_KEY_SECRET.getBytes();
		SecretKey key = Keys.hmacShaKeyFor(keyBytes);

		String compactJws = Jwts.builder().setHeader(headerClaims)
				.signWith(key)
				.claim("jti", StringeeKeys.STRINGEE_KEY_ID + "-" + System.currentTimeMillis())
				.claim("iss", StringeeKeys.STRINGEE_KEY_ID)
				.claim("userId", userId)
				.setExpiration(exp)
				.compact();
		return compactJws;
	}

}
