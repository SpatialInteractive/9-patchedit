package net.rcode.npedit;

/**
 * Base class to edit an image
 * @author stella
 *
 */
public abstract class EditCommand {
	public abstract void performEdit(NinePatchImage npi) throws Exception;
}
