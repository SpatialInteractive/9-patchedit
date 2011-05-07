package net.rcode.npedit;

import java.awt.image.BufferedImage;

/**
 * Clears the 9patch border
 * @author stella
 *
 */
public class ClearEditCommand extends EditCommand {

	@Override
	public void performEdit(NinePatchImage npi) throws Exception {
		BufferedImage img=npi.image;
		int blank=0x00000000;
		int h=img.getHeight();
		int w=img.getWidth();
		for (int x=0; x<w; x++) {
			img.setRGB(x, 0, blank);
			img.setRGB(x, h-1, blank);
		}
		
		for (int y=0; y<h; y++) {
			img.setRGB(0, y, blank);
			img.setRGB(w-1, y, blank);
		}
	}
	
	public String toString() {
		return "Clear 9 patch border area to default";
	}
}
