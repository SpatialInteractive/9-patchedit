package net.rcode.npedit;

public class SetScaleYEditCommand extends RangeEditCommand {

	@Override
	protected void performEdit(NinePatchImage npi, int index) {
		npi.image.setRGB(0, index, 0xff000000);
	}
	
	@Override
	public String toString() {
		return "Set y scale points " + rangeDescription();
	}
}