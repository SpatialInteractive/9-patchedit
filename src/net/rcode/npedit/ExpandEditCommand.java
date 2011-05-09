package net.rcode.npedit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpandEditCommand extends EditCommand {
	public static final Pattern SPEC_PATTERN=Pattern.compile("([0-9]+)\\s*[x,]\\s*([0-9]+)");
	public int targetWidth;
	public int targetHeight;
	
	public void parse(String spec) {
		Matcher m=SPEC_PATTERN.matcher(spec.trim());
		if (!m.matches()) {
			throw new IllegalArgumentException("Expected expand argument to be of form 20x30 or 20,30");
		}
		
		targetWidth=Integer.parseInt(m.group(1));
		targetHeight=Integer.parseInt(m.group(2));
	}
	
	@Override
	public void performEdit(EditContext context, NinePatchImage npi) throws Exception {
		npi.expand(targetWidth, targetHeight);
	}

	@Override
	public String toString() {
		return "Expand 9-patch to " + targetWidth + "x" + targetHeight;
	}
}
