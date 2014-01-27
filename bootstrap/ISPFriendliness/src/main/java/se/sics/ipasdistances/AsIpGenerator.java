package se.sics.ipasdistances;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.MatchResult;

public class AsIpGenerator {
	private static int RANDOM_SHIFT = 10;
	private static AsIpGenerator genInstance = null;
	private ArrayList<String> ipMasks = null;
	private Random innerRandom = null;
	private Random outerRandom = null;

	public static AsIpGenerator getInstance(long seed) {
		if (genInstance == null) {
			genInstance = new AsIpGenerator(seed);
		}
		return genInstance;
	}

	private AsIpGenerator(long seed) {
		innerRandom = new Random(seed);
		outerRandom = new Random(seed + RANDOM_SHIFT);
		ipMasks = new ArrayList<String>();
		try {
			InputStream is = 
                                Thread.currentThread().getContextClassLoader().getResourceAsStream("data/routes.txt");
			Scanner sc = new Scanner(is);
			while (sc.hasNext()) {
				String line = sc.nextLine();
				if (!line.isEmpty()) {
					ipMasks.add(line);
				}
			}
		} catch (Exception e) {
			System.err.println("Exception when loading data:" + e.getMessage());
		}
	}

	public static void main(String[] args) {
		AsIpGenerator gen = AsIpGenerator.getInstance(0);
		for (int i = 0; i < 50; i++) {
			System.out.println(gen.generateIP());
		}
	}

	public InetAddress generateIP() {
		int mask = 0;
		InetAddress newAddress = null;
		while (true) {
			try {
				String strAddress = "";
				int index = outerRandom.nextInt(ipMasks.size());
				String line = ipMasks.get(index);
				// Parse line
				Scanner s = new Scanner(line);
				s.findInLine("([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})/(\\d+) (\\d+)");
				MatchResult matchRes = s.match();
				mask = 32 - Integer.parseInt(matchRes.group(5));
				int currentMask = 32;
				for (int i = 0; i < 4; i++) {
					int res = currentMask - mask;
					String localString = matchRes.group(i + 1);
					String tempBin = Integer.toBinaryString(Integer.parseInt(localString));
					if (res < 8) {
						byte[] bits = tempBin.getBytes();
						// Fill missing bytes
						if (bits.length < 8) {
							int len = 8 - bits.length;
							byte[] bitsTemp = new byte[8];
							int k = 0;
							for (int j = 0; j < 8; j++) {
								if (j < len)
									bitsTemp[j] = (byte) '0';
								else {
									bitsTemp[j] = bits[k];
									k++;
								}
							}
						}
						for (int j = res + 1; j < 8; j++) {
							bits[j] = (innerRandom.nextBoolean() ? (byte) '1' : (byte) '0');
						}
						tempBin = new String(bits);
						mask = (4 - i) * 8;
					}
					strAddress += (i == 0 ? "" : ".") + Integer.parseInt(tempBin, 2);
					currentMask = currentMask - 8;
				}
				newAddress = InetAddress.getByName(strAddress);
				break;
			} catch (Exception e) {
				continue;
			}
		}
		return newAddress;
	}
}
