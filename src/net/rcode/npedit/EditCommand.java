package net.rcode.npedit;

/**
 * Base class to edit an image
 * @author stella
 *
 */
public abstract class EditCommand {
	public abstract void performEdit(EditContext context, NinePatchImage npi) throws Exception;
}
