package net.rcode.npedit;

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

public class ImageUtil {
	public static ImageReader getPngReader() {
		Iterator<ImageReader> iter=ImageIO.getImageReadersByFormatName("png");
		if (!iter.hasNext()) {
			throw new Error("No PNG image reader found");
		}
		return iter.next();
	}
	
	public static ImageWriter getPngWriter() {
		return ImageIO.getImageWriter(getPngReader());
	}
}
