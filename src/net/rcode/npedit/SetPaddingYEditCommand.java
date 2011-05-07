package net.rcode.npedit;

public class SetPaddingYEditCommand extends RangeEditCommand {

	@Override
	protected void performEdit(NinePatchImage npi, int index) {
		npi.image.setRGB(npi.image.getWidth()-1, index, 0xff000000);
	}
	
	@Override
	public String toString() {
		return "Set y padding points " + rangeDescription();
	}
}