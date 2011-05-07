package net.rcode.npedit;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * A nine patch image loaded into memory
 * @author stella
 *
 */
public class NinePatchImage {
	public static final String NINE_PATCH_SUFFIX=".9.png";
	public static final String PNG_SUFFIX=".png";
	
	public boolean isNinePatch;
	public File plainFile;
	public File ninePatchFile;
	public BufferedImage image;
	
	public NinePatchImage() {
	}
	
	public void ensureNinePatch() {
		if (isNinePatch) return;
		
		// Otherwise, we need to add a border
		BufferedImage newImage=new BufferedImage(image.getWidth()+2, image.getHeight()+2, image.getType());
		newImage.getRaster().setRect(1, 1, image.getRaster());
		image=newImage;
		isNinePatch=true;
	}
	
	public void ensurePlain() {
		if (!isNinePatch) return;
		
		// Otherwise, we need to remove 2 rows and 2 columns
		BufferedImage newImage=new BufferedImage(image.getWidth()-2, image.getHeight()-2, image.getType());
		Raster src=image.getRaster().createChild(1, 1, newImage.getWidth(), newImage.getHeight(), 
				0, 0, null);
		newImage.getRaster().setRect(src);
		image=newImage;
		isNinePatch=false;
	}
	
	public static NinePatchImage load(File source) throws IOException {
		NinePatchImage ret=new NinePatchImage();
		ImageReader reader=ImageUtil.getPngReader();	
		ImageInputStream in=ImageIO.createImageInputStream(source);
		reader.setInput(in);
		ret.image=reader.read(0);
		reader.dispose();
		in.close();

		// Detect if nine patch or not
		String name=source.getName();
		if (name.toLowerCase().endsWith(NINE_PATCH_SUFFIX)) {
			ret.isNinePatch=true;
			ret.ninePatchFile=source;
			name=name.substring(0, name.length()-NINE_PATCH_SUFFIX.length()) + PNG_SUFFIX;
			ret.plainFile=new File(source.getParentFile(), name);
		} else {
			ret.isNinePatch=false;
			ret.plainFile=source;
			name=name.substring(0, name.length()-PNG_SUFFIX.length()) + NINE_PATCH_SUFFIX;
			ret.ninePatchFile=new File(source.getParentFile(), name);
		}
		
		return ret;
	}

	public File getOutputFile() {
		File outputFile;
		if (isNinePatch) outputFile=ninePatchFile;
		else outputFile=plainFile;
		return outputFile;
	}
	
	public void save(File outputFile) throws IOException {
		ImageOutputStream out=ImageIO.createImageOutputStream(outputFile);
		try {
			ImageWriter writer=ImageUtil.getPngWriter();
			writer.setOutput(out);
			writer.write(image);
			writer.dispose();
		} finally {
			out.close();
		}
	}

	public static boolean validatePixel(int pixel) {
		return pixel==0 || pixel==0xff000000;
	}
}
