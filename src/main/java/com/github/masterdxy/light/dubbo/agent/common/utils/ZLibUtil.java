package com.github.masterdxy.light.dubbo.agent.common.utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * ZLib压缩工具
 * 
 * @version 1.0
 * @since 1.0
 */
public class ZLibUtil {

	//fixme 大小限制到2M
	private final static int DECOMPRESS_MAX_SIZE=1024*500;//最大支持500M的数据解压缩
	/**
	 * 压缩
	 * 
	 * @param data
	 *            待压缩数据
	 * @return byte[] 压缩后的数据
	 */
	public static byte[] compress(byte[] data) {
		if(data==null || data.length<=0){
			return null;
		}
		byte[] output = new byte[0];
		Deflater compresser = new Deflater();
		compresser.reset();
		compresser.setLevel(Deflater.BEST_COMPRESSION);
		compresser.setInput(data);
		compresser.finish();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		try {
			byte[] buf = new byte[1024];
			while (!compresser.finished()) {
				int i = compresser.deflate(buf);
				bos.write(buf, 0, i);
			}
			output = bos.toByteArray();
		} catch (Exception e) {
			output = data;
			e.printStackTrace();
		} finally {
			try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		compresser.end();
		return output;
	}

	/**
	 * 压缩
	 * 
	 * @param data
	 *            待压缩数据
	 * 
	 * @param os
	 *            输出流
	 */
	public static void compress(byte[] data, OutputStream os) {
		if(data==null || data.length<=0){
			return;
		}
		DeflaterOutputStream dos = new DeflaterOutputStream(os);
		try {
			dos.write(data, 0, data.length);
			dos.finish();
			dos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解压缩
	 * 
	 * @param data
	 *            待压缩的数据
	 * @return byte[] 解压缩后的数据
	 */
	public static byte[] decompress(byte[] data) {
		if(data==null || data.length<=0){
			return null;
		}
		Inflater decompresser = new Inflater();
		decompresser.setInput(data);
		ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
		try {
			byte[] buf = new byte[2048];
			int j=0;
			while (!decompresser.finished()) {
				int i = decompresser.inflate(buf);
				o.write(buf, 0, i);
				if(j++>=DECOMPRESS_MAX_SIZE){
					break;
				}
			}
		} catch (Exception e) {
		} finally {
			decompresser.end();
			decompresser=null;
		}
		return o.toByteArray();
	}
	/**
	 * 已知原始长度的解压缩
	 * @param data
	 * @param length
	 * @return
	 */
	public static byte[] decompress(byte[] data,int length) {
		if(data==null || data.length<=0){
			return null;
		}
		if(length<=0){
			return decompress(data);
		}
		Inflater decompresser = new Inflater();
		decompresser.setInput(data);
		ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
		try {
			byte[] buf = new byte[length];
			if (!decompresser.finished()) {
				int i = decompresser.inflate(buf);
				o.write(buf, 0, i);
			}
		} catch (Exception e) {
		} finally {
			decompresser.end();
			decompresser=null;
		}
		return o.toByteArray();
	}

	/**
	 * 解压缩
	 * 
	 * @param is
	 *            输入流
	 * @return byte[] 解压缩后的数据
	 */
	public static byte[] decompress(InputStream is) {
		InflaterInputStream iis = new InflaterInputStream(is);
		ByteArrayOutputStream o = new ByteArrayOutputStream(1024);
		try {
			int i = 2048;
			byte[] buf = new byte[i];
			int j=0;
			while ((i = iis.read(buf, 0, i)) > 0) {
				o.write(buf, 0, i);
				if(j++>=DECOMPRESS_MAX_SIZE){
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return o.toByteArray();
	}
}