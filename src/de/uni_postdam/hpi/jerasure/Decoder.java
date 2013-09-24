package de.uni_postdam.hpi.jerasure;

import java.io.File;

import de.uni_postdam.hpi.utils.FileUtils;

public class Decoder {

	File original = null;
	int k, m, w;
	
	File[] k_parts, m_parts;
	
	boolean[] erasures = null;
	
	public Decoder(File file, int k, int m, int w) {
		this.original = file;
		this.k = k;
		this.m = m;
		this.w = w;
		
		this.k_parts = FileUtils.collectFiles(file.getAbsolutePath(), "k", k);
		this.m_parts = FileUtils.collectFiles(file.getAbsolutePath(), "m", m);
		this.erasures = new boolean[k+m];
		
		updateErasures();
	}

	private void updateErasures(){
		if(erasures == null) 
			throw new RuntimeException("erasures was null!");
		
		int c = 0;
		for(File part: k_parts){
			erasures[c++] = !part.exists();
		}
		for(File part: m_parts){
			erasures[c++] = !part.exists();
		}
	}

	public boolean isValid(){
		updateErasures();
		int c = 0;
		for(boolean erased: erasures){
			if(erased)
				c++;
		}
		
		return c <= this.m;
	}
	
}
