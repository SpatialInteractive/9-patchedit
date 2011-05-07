package net.rcode.npedit;

import java.util.ArrayList;
import java.util.List;

/**
 * Perform edits over a range of pixels
 * @author stella
 *
 */
public abstract class RangeEditCommand extends EditCommand {
	private List<int[]> ranges=new ArrayList<int[]>();
	
	public RangeEditCommand parse(String spec) {
		String[] parts=spec.split("\\s*\\,\\s*");
		try {
			for (String part: parts) {
				part=part.trim();
				int dashPos=part.indexOf("-");
				if (dashPos>0) {
					String start=part.substring(0,dashPos);
					String end=part.substring(dashPos+1);
					add(Integer.parseInt(start.trim()), Integer.parseInt(end.trim()));
				} else {
					add(Integer.parseInt(part));
				}
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("The range specifier " + spec + " is not valid");
		}
		return this;
	}
	
	public void add(int point) {
		ranges.add(new int[] { point });
	}
	
	public void add(int start, int end) {
		ranges.add(new int[] { start, end });
	}
	
	@Override
	public void performEdit(NinePatchImage npi) throws Exception {
		for (int[] range: ranges) {
			if (range.length==1) {
				performEdit(npi, range[0]);
			} else {
				for (int i=range[0]; i<range[1]; i++) {
					performEdit(npi, i);
				}
			}
		}
	}
	
	protected abstract void performEdit(NinePatchImage npi, int index);
	
	public String rangeDescription() {
		StringBuilder sb=new StringBuilder();
		for (int[] range: ranges) {
			if (sb.length()>0) sb.append(",");
			if (range.length==1) sb.append(range[0]);
			else sb.append(range[0]).append("-").append(range[1]);
		}
		
		return sb.toString();
	}
}
