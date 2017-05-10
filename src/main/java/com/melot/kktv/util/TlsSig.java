package com.melot.kktv.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.tls.sigcheck.tls_sigcheck;

/**
 * 生成腾讯验证签名
 * @author 
 *
 */
public class TlsSig {
	
	private static Logger logger = Logger.getLogger(TlsSig.class);
	
	private static String ec_key;
	
	public static void init(String pempath, String sopath) {
		
		System.load(sopath);
		
		File file = new File(pempath);
		StringBuilder sb = new StringBuilder();
		String s = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			while ((s = br.readLine()) != null) {
				sb.append(s + "\n");
			}
			br.close();
		} catch (FileNotFoundException e) {
			logger.error("can't find the file", e);
		} catch (IOException e) {
			logger.error("can't find the file", e);
		}
		ec_key = sb.toString();
				
	}
	
	/**
	 * 生成第三方腾讯验证签名
	 * @param userid
	 * @return
	 */
	public synchronized static String sig_gen_3rd(int userid) {
		String sig = null;
		try {
			tls_sigcheck tls = new tls_sigcheck();
			int ret = tls.tls_gen_signature_ex2(ConfigHelper.getSdk_appid(), userid + "", ec_key);
			if (ret == 0) {
				sig = tls.getSig();
			} else {
				logger.error("fail to generate signature, return error, code " + ret + " , msg " + tls.getErrMsg());
			}
		} catch (Exception e) {
			logger.error("fail to generate signature", e);
		}

		return sig;
	}
}
