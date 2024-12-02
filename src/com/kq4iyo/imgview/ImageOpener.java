package com.kq4iyo.imgview;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HexFormat;
import java.util.zip.Inflater;

public class ImageOpener extends Thread {

	private File imageFile;
	public boolean isReady = false;
	
	public PLTEChunk palletChunk;
	public Color[] pixelColors;
	
	public int imageWidth = 0, imageHeight = 0;
	
	public ImageOpener(File file) {
		imageFile = file;
		
		if(!imageFile.exists())
		{
			System.err.println("File \"" + imageFile.getAbsolutePath() + "\" Does Not Exist!");
			System.exit(1);
		}
		
		if(!imageFile.canRead())
		{
			System.err.println("No Permission To Open File \"" + imageFile.getAbsolutePath() + "\"!");
			System.exit(1);
		}
		
		this.start();
	}
	
	public void run() {
		String[] parts = imageFile.getName().split("\\.");
		if(parts.length == 0)
		{
			System.err.println("File \"" + imageFile.getAbsolutePath() + "\" Has No File Extension!");
			System.exit(1);
		}
		
		String extension = parts[parts.length-1].toUpperCase();
		
		switch(extension) {
		case "PNG":
		{
			decodePNG();
			break;
		}
		default:{
			System.err.println("File \"" + imageFile.getAbsolutePath() + "\" Has An Unknown Extension!");
			break;
		}
		}
	}
	
	private void decodePNG() {
		try {
			
			byte[] bytes = Files.readAllBytes(Paths.get(imageFile.getAbsolutePath()));
			
			byte[] header = new byte[] {
					bytes[0],bytes[1],bytes[2],bytes[3],
					bytes[4],bytes[5],bytes[6],bytes[7]
			};
			String headerHex = HexFormat.of().formatHex(header).toUpperCase();
			
			if(!headerHex.equals("89504E470D0A1A0A"))
			{
				System.err.println("File \"" + imageFile.getAbsolutePath() + "\" Has PNG Extension But Is Not A PNG File!");
				System.exit(1);
			}
			
			System.out.println("Validated Is PNG");
			
			PNGChunk firstChunk = PNGChunk.getNextChunk(8, bytes);
			IHDRChunk headerChunk = IHDRChunk.fromChunk(firstChunk);
			
			System.out.println("Image Width: " + headerChunk.width + "\nImage Height: " + headerChunk.height);
			this.imageWidth = headerChunk.width;
			this.imageHeight = headerChunk.height;
		
			this.pixelColors = new Color[imageWidth * imageHeight];
			int pixelColorIndex = 0;
			
			PNGChunk[] chunks = new PNGChunk[255];
			chunks[0] = firstChunk;
			
			boolean issRGB = false;
			
			PNGChunk[] IDATchunks = new PNGChunk[1024];
			int IDATcounter = 0;
			
			int index = 1;
			PNGChunk currentChunk = firstChunk;
			while(!(currentChunk = PNGChunk.getNextChunk(currentChunk.nextOffset, bytes)).Type.equals("IEND")) {
				System.out.println(currentChunk.Type);
				chunks[index] = currentChunk;
				index++;
				
				if(currentChunk.Type.equals("sRGB"))
				{
					sRGBChunk colorChunk = sRGBChunk.fromChunk(currentChunk);
					System.out.println("color value: " + colorChunk.renderIntent);
					issRGB = true;
				}else if(currentChunk.Type.equals("gAMA"))
				{
					gAMAChunk gamaChunk = gAMAChunk.fromChunk(currentChunk);
					System.out.println("gama value: " + gamaChunk.imageGama);
				}else if(currentChunk.Type.equals("pHYs")) {
					pHYsChunk scaleChunk = pHYsChunk.fromChunk(currentChunk);
					System.out.println("ScaleX: " + scaleChunk.ppuX);
					System.out.println("ScaleY: " + scaleChunk.ppuY);
					System.out.println("Unit: " + scaleChunk.unit);
				}else if(currentChunk.Type.equals("PLTE")) {
					PLTEChunk palletChunk = PLTEChunk.fromChunk(currentChunk);
					this.palletChunk = palletChunk;
				}else if(currentChunk.Type.equals("IDAT")) {
					IDATchunks[IDATcounter] = currentChunk;
					IDATcounter++;
				}
			}
			
			int size = 0;
			byte[] fulldata;
			for(PNGChunk chunk:IDATchunks) {
				if(chunk == null)
					break;
				size += chunk.Length;
			}
			
			fulldata = new byte[size];
			int pos = 0;
			
			for(PNGChunk chunk:IDATchunks) {
				if(chunk == null)
					break;
				System.arraycopy(chunk.data, 0, fulldata, pos, chunk.Length);
				pos += chunk.Length;
			}
			
			
			System.out.println("IDAT Length: " + fulldata.length);
			
			Inflater inflater = new Inflater();
			inflater.setInput(fulldata);
			
			byte[] buffer = new byte[2048];
			int decompressedDataLength = 0;
			byte[] output = new byte[0];
			
			while(!inflater.finished())
			{
				try {
					int count = inflater.inflate(buffer);
					byte[] temp = new byte[output.length + count];
					System.arraycopy(output, 0, temp, 0, output.length);
					System.arraycopy(buffer, 0, temp, output.length, count);
					output = temp;
					decompressedDataLength += count;
				}catch(Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			System.out.println("COlor Type: " + headerChunk.colortype);
			
			
			inflater.end();
			
			int bytesPerScanline = 1 + (this.imageWidth * headerChunk.bitdepth)/8; // filter byte every scanline + pixel count * bits per pixel
			System.out.println("Expected Bytes per scanline: " + bytesPerScanline);
			
			for(int i = 0; i < output.length; i += bytesPerScanline) {
				byte filter = output[i];
				byte[] pixeldata = new byte[bytesPerScanline-1];
				
				for(int j = 0; j < pixeldata.length; j++) {
					pixeldata[j] = output[i+j+1];
				}
				
				System.out.println("FILTER: " + filter);
				
				if(filter == 0)
				{
					if(issRGB && headerChunk.colortype != 3) {
						for(int j = 0; j < pixeldata.length/(headerChunk.bitdepth/8); j++) {
						
						}
					}else if(headerChunk.colortype == 2){
						for(int j = 0; j < pixeldata.length; j += 3) {
							int r = pixeldata[j] & 0xFF;
							int g = pixeldata[j+1] & 0xFF;
							int b = pixeldata[j+2] & 0xFF;
							
							Color c = new Color(r,g,b);
							this.pixelColors[pixelColorIndex] = c;
							pixelColorIndex++;
						}
					}else {
						System.out.println(pixeldata.length + "/(" + headerChunk.bitdepth + "/8) = " + (pixeldata.length /(headerChunk.bitdepth/8)));
						for(int j = 0; j < pixeldata.length/(headerChunk.bitdepth/8); j++) {
							int colorIndex = Byte.toUnsignedInt(pixeldata[j]);
							Color c = this.palletChunk.pallet[colorIndex];
							if(c == null)
								c = Color.black;
							this.pixelColors[pixelColorIndex] = c;
							pixelColorIndex++;
						}
					}
				}else {
					System.err.println("Unknown Filter Format!");
				}
			}
			
			this.isReady = true;
			try {
				this.join();
			}catch(InterruptedException e) {
				e.printStackTrace();
			}
			
		}catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public boolean ready() {
		return this.isReady;
	}
}

class PNGChunk {
	
	public int Length, CRC, nextOffset;
	public String Type;
	public byte[] data;
	
	public boolean isCritical = false, isPublic = true, isStandard = true, safeToCopy = false;
	
	public static PNGChunk getNextChunk(int startIndex, byte[] bytes) {
		PNGChunk chunk = new PNGChunk();
		
		byte[] length = new byte[] {bytes[startIndex], bytes[startIndex + 1], bytes[startIndex + 2], bytes[startIndex + 3]};
		byte[] type = new byte[] {bytes[startIndex + 4], bytes[startIndex + 5], bytes[startIndex + 6], bytes[startIndex + 7]};
		
		chunk.Length = ByteBuffer.wrap(length).getInt();
		chunk.Type = new String(type, StandardCharsets.US_ASCII);
		
		String isChunkCriticalCheck = String.valueOf(chunk.Type.charAt(0));
		if(isChunkCriticalCheck.toUpperCase().equals(isChunkCriticalCheck))
			chunk.isCritical = true;
		
		String isChunkPublicCheck = String.valueOf(chunk.Type.charAt(1));
		if(!isChunkPublicCheck.toUpperCase().equals(isChunkPublicCheck))
			chunk.isPublic = false;
		
		String thirdLetterCheck = String.valueOf(chunk.Type.charAt(2));
		if(!thirdLetterCheck.toUpperCase().equals(thirdLetterCheck))
			chunk.isStandard = false;
		
		String safeToCopyCheck = String.valueOf(chunk.Type.charAt(3));
		if(!safeToCopyCheck.toUpperCase().equals(safeToCopyCheck))
			chunk.safeToCopy = true;
		
		
		chunk.data = new byte[chunk.Length];
		
		for(int i = startIndex+8; i < startIndex + 8 + chunk.Length; i++) {
			chunk.data[(i - startIndex - 8)] = bytes[i];
		}
		
		startIndex = startIndex + 8 + chunk.Length;
		byte[] crcData = new byte[] {bytes[startIndex], bytes[startIndex + 1], bytes[startIndex + 2], bytes[startIndex + 3]};
		
		chunk.CRC = ByteBuffer.wrap(crcData).getInt();
		
		chunk.nextOffset = startIndex + 4;
		
		return chunk;
	}
	
}

class IHDRChunk {
	
	public int width, height;
	public byte bitdepth, colortype, compression, filter, interlace;
	
	public static IHDRChunk fromChunk(PNGChunk oldChunk) {
		IHDRChunk chunk = new IHDRChunk();
		
		if(!oldChunk.Type.equals("IHDR"))
		{
			chunk.width = -1;
			chunk.height = -1;
			return chunk;
		}
		
		byte[] data = oldChunk.data;
		
		byte[] width = new byte[] {data[0],data[1],data[2],data[3]};
		byte[] height = new byte[] {data[4],data[5],data[6],data[7]};
		
		chunk.bitdepth = data[8];
		chunk.colortype = data[9];
		chunk.compression = data[10];
		chunk.filter = data[11];
		chunk.interlace = data[12];
		
		chunk.width = ByteBuffer.wrap(width).getInt();
		chunk.height = ByteBuffer.wrap(height).getInt();
		
		return chunk;
	}
	
}

class sRGBChunk {
	
	public byte renderIntent = 0;
	
	public static sRGBChunk fromChunk(PNGChunk oldChunk) {
		sRGBChunk chunk = new sRGBChunk();
		
		if(!oldChunk.Type.equals("sRGB"))
		{
			chunk.renderIntent = -1;
			return chunk;
		}
		
		chunk.renderIntent = oldChunk.data[0];
		
		return chunk;
	}
}

class gAMAChunk {
	
	public int imageGamaRaw = 0;
	public double imageGama = 0.0f;
	
	public static gAMAChunk fromChunk(PNGChunk oldChunk) {
		gAMAChunk chunk = new gAMAChunk();
		
		if(!oldChunk.Type.equals("gAMA"))
		{
			chunk.imageGamaRaw = -1;
			return chunk;
		}
		
		byte[] data = oldChunk.data;
		byte[] gamaraw = new byte[] {data[0],data[1],data[2],data[3]};
		
		chunk.imageGamaRaw = ByteBuffer.wrap(gamaraw).getInt();
		chunk.imageGama = chunk.imageGamaRaw / 100000;
		
		return chunk;
	}
}

class pHYsChunk {
	
	public int ppuX, ppuY;
	public byte unit = 0;
	
	public static pHYsChunk fromChunk(PNGChunk oldChunk) {
		pHYsChunk chunk = new pHYsChunk();
		
		if(!oldChunk.Type.equals("pHYs"))
		{
			chunk.ppuX = -1;
			chunk.ppuY = -1;
			return chunk;
		}

		byte[] data = oldChunk.data;
		
		byte[] ppuXdata = new byte[] {data[0],data[1],data[2],data[3]};
		byte[] ppuYdata = new byte[] {data[4],data[5],data[6],data[7]};
		chunk.unit = data[8];
		
		chunk.ppuX = ByteBuffer.wrap(ppuXdata).getInt();
		chunk.ppuY = ByteBuffer.wrap(ppuYdata).getInt();
		
		return chunk;
	}
}

class PLTEChunk {
	
	public int numColors = 0;
	public Color[] pallet;
	
	public static PLTEChunk fromChunk(PNGChunk oldChunk) {
		PLTEChunk chunk = new PLTEChunk();
		
		if(!oldChunk.Type.equals("PLTE") || oldChunk.Length % 3 != 0)
		{
			chunk.numColors = -1;
			return chunk;
		}
		
		byte[] data = oldChunk.data;
		chunk.numColors = data.length/3;
		chunk.pallet = new Color[chunk.numColors];
		
		for(int i = 0; i < data.length; i += 3) {
			int red = data[i] & 0xFF;
			int green = data[i+1] & 0xFF;
			int blue = data[i+2] & 0xFF;
			
			chunk.pallet[i/3] = new Color(red,green,blue);
		}
		
		return chunk;
	}
}