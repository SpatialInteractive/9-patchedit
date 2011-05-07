package net.rcode.npedit;

import java.awt.image.BufferedImage;
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
	File source;
	BufferedImage image;
	
	public NinePatchImage() {
	}
	
	public static NinePatchImage load(File source) throws IOException {
		NinePatchImage ret=new NinePatchImage();
		
		ImageReader reader=ImageUtil.getPngReader();

		
		ImageInputStream in=ImageIO.createImageInputStream(source);
		reader.setInput(in);
		
		ret.source=source;
		ret.image=reader.read(0);
		reader.dispose();
		in.close();
		
		return ret;
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
}
