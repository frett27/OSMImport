package com.osmimport.parsing.pbf.actors.messages;

import crosby.binary.Fileformat;

public class BlobMessageWithNo {

	public BlobMessageWithNo(Fileformat.Blob blob, int cpt)
	{
		this.blob = blob;
		this.cpt = cpt;
	}
	
	public Fileformat.Blob blob;
	public int cpt;
	
}
