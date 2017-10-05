package com.pt.vault;

import java.io.UnsupportedEncodingException;
import java.util.Random;

import org.junit.Test;

import com.pt.vault.hasher.HashEngine;
import com.pt.vault.hasher.PlainHasherImpl;

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
}
