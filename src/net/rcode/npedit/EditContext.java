package net.rcode.npedit;

public interface EditContext {
	public void logCommandDetail(String msg);
	public void applyCommand(NinePatchImage npi, EditCommand cmd) throws Exception;
}
