package net.rcode.npedit;

public class SetScaleXEditCommand extends RangeEditCommand {

	@Override
	protected void performEdit(NinePatchImage npi, int index) {
		npi.image.setRGB(index, 0, 0xff000000);
	}
	
	@Override
	public String toString() {
		return "Set x scale points " + rangeDescription();
	}
}
