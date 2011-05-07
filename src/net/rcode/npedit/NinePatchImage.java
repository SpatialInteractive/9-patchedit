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
	public static final int MODE_SCALEX=0;
	public static final int MODE_SCALEY=1;
	public static final int MODE_PADDINGX=2;
	public static final int MODE_PADDINGY=3;
	
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
		int alpha=pixel >>> 24;
		return alpha==0 || alpha==0xff;
	}

	public static boolean isSetPixel(int pixel) {
		int alpha=pixel >>> 24;
		if (alpha==0) return false;
		else if (alpha==0xff) return true;
		else return false;
	}
	
	public int[] getMarkers(int mode) {
		if (!isNinePatch) return new int[0];
		
		int w=image.getWidth();
		int h=image.getHeight();
		int[] markers;
		int count=0;
		if (mode==MODE_SCALEX || mode==MODE_PADDINGX) {
			int y;
			if (mode==MODE_SCALEX) y=0;
			else y=h-1;
			markers=new int[w];
			
			for (int x=0; x<w; x++) {
				if (isSetPixel(image.getRGB(x, y))) {
					markers[count++]=x;
				}
			}
		} else if (mode==MODE_SCALEY || mode==MODE_PADDINGY) {
			int x;
			if (mode==MODE_SCALEY) x=0;
			else x=w-1;
			markers=new int[h];
			
			for (int y=0; y<h; y++) {
				if (isSetPixel(image.getRGB(x, y))) {
					markers[count++]=y;
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
		
		int[] ret=new int[count];
		System.arraycopy(markers, 0, ret, 0, count);
		
		return ret;
	}
	
	/**
	 * Convert image to 9-patch and expand it per the scale bars
	 * @param targetWidth
	 * @param targetHeight
	 */
	public void expand(int targetWidth, int targetHeight) {
		ensureNinePatch();
		
		int addCols=targetWidth - image.getWidth();
		int addRows=targetWidth - image.getHeight();
		
		if (addCols<0 || addRows<0) {
			throw new IllegalArgumentException("Attempt to expand image by a negative amount");
		}
		
		int[] colMarkers=getMarkers(MODE_SCALEX);
		int[] rowMarkers=getMarkers(MODE_SCALEY);
		BufferedImage dest=new BufferedImage(targetWidth, targetHeight, image.getType());
		
		int colLoops=0, colRem=0;
		if (colMarkers.length>0) {
			colLoops=addCols/colMarkers.length;
			colRem=addCols%colMarkers.length;
		}
		
		int rowLoops=0, rowRem=0;
		if (rowMarkers.length>0) {
			rowLoops=addRows/rowMarkers.length;
			rowRem=addRows%rowMarkers.length;
		}

		//System.out.format("Adding colLoops=%s, colRem=%s, rowLoops=%s, rowRem=%s\n", colLoops, colRem, rowLoops, rowRem);
		
		int index;
		
		// Copy rows
		int ydest=0;
		int ysrc=0;
		for (index=0; index<rowMarkers.length; index++) {
			int y=rowMarkers[index];
			// Copy fixed rows
			for (; ysrc<y; ysrc++) {
				copyRow(image, ysrc, dest, ydest++);
			}
			
			// Copy this row up to loop times
			for (int c=0; c<rowLoops; c++) {
				copyRow(image, y, dest, ydest++);
			}
			
			// Copy one more if there is a remainder
			if (rowRem>0) {
				copyRow(image, y, dest, ydest++);
				rowRem--;
			}
			
			ysrc=y;
		}
		
		// Copy remaining rows
		for (; ysrc<image.getHeight(); ysrc++) {
			copyRow(image, ysrc, dest, ydest++);
		}
		
		// Copy columns (backwards)
		int xdest=targetWidth-1;
		int xsrc=image.getWidth()-1;
		for (index=colMarkers.length-1; index>=0; index--) {
			int x=colMarkers[index];
			
			// Copy fixed cols
			for (; xsrc>x; xsrc--) {
				copyCol(dest, xsrc, dest, xdest--);
			}
			
			// Copy this col up to loop times
			for (int c=0; c<colLoops; c++) {
				copyCol(dest, x, dest, xdest--);
			}
			
			// Copy one more for remainder
			if (colRem>0) {
				copyCol(dest, x, dest, xdest--);
				colRem--;
			}
			
			xsrc=x;
		}
		
		// Copy remaining cols
		for (; xsrc>=0; xsrc--) {
			copyCol(dest, xsrc, dest, xdest--);
		}
		
		this.image=dest;
	}

	private void copyCol(BufferedImage src, int xsrc, BufferedImage dest,
			int xdest) {
		//System.out.println("copyCol(" + xsrc + " -> " + xdest + ")");
		
		Raster srcRaster=src.getRaster().createChild(xsrc, 0, 1, Math.min(src.getHeight(), dest.getHeight()), 
				0, 0, null);
		dest.getRaster().setDataElements(xdest, 0, srcRaster);
	}

	private void copyRow(BufferedImage src, int ysrc, BufferedImage dest,
			int ydest) {
		Raster srcRaster=src.getRaster().createChild(0, ysrc, Math.min(src.getWidth(), dest.getWidth()), 
				1, 0, 0, null);
		dest.getRaster().setDataElements(0, ydest, srcRaster);
	}
}
