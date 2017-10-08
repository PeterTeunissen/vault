package com.pt.vault;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

import com.pt.vault.hasher.HashEngine;
import com.pt.vault.hasher.PlainHasherImpl;
import com.pt.vault.hasher.SecretHasherImpl;

public class HashTest {

	@Test
	public void bigStringTest() throws IllegalStateException, UnsupportedEncodingException {
		Long s = System.currentTimeMillis();
		StringBuilder keys = new StringBuilder();
		for (int i = 0; i != 10000; i++) {
			keys.append((new Random()).nextLong()).append(",");
		}
		System.out.println("Done creating String:" + (System.currentTimeMillis() - s));
		// SecretHasher h = new SecretHasher("PeterTeunissen");
		HashEngine h = new PlainHasherImpl();
		s = System.currentTimeMillis();
		String e = h.getSecretHash(keys.toString());
		System.out.println("Done making hash:" + (System.currentTimeMillis() - s));

	}

	@Test
	public void testCommonsHasher() {
		HashCodeBuilder bldr = new HashCodeBuilder(17, 37);
		bldr.append("a");
		bldr.append("b");
		int ab = bldr.toHashCode();

		bldr = new HashCodeBuilder(17, 37);
		bldr.append("b");
		bldr.append("a");
		int ba = bldr.toHashCode();

		System.out.println(ab + " " + ba);
	}

	@Test
	public void testSecretHasher() throws IllegalStateException, UnsupportedEncodingException {
		SecretHasherImpl h = new SecretHasherImpl("OK");
		System.out.println(h.getSecretHash("one"));
		System.out.println(h.getSecretHash("one"));
	}
}
