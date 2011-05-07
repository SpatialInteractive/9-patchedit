package net.rcode.npedit;

public class StripEditCommand extends EditCommand {

	@Override
	public void performEdit(NinePatchImage npi) throws Exception {
		npi.ensurePlain();
	}

	@Override
	public String toString() {
		return "Strip 9-patch border and convert to standard png";
	}
}
