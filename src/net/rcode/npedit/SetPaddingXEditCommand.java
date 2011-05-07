package net.rcode.npedit;

public class SetPaddingXEditCommand extends RangeEditCommand {

	@Override
	protected void performEdit(NinePatchImage npi, int index) {
		npi.image.setRGB(index, npi.image.getHeight()-1, 0xff000000);
	}
	
	@Override
	public String toString() {
		return "Set x padding points " + rangeDescription();
	}
}