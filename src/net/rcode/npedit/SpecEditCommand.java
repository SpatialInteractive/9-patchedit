package net.rcode.npedit;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Looks for a ".spec" file with the same name as the source file.  This is
 * just a java properties file with the following properties:
 * <ul>
 * <li>sx
 * <li>sy
 * <li>px
 * <li>py
 * </ul>
 * 
 * @author stella
 *
 */
public class SpecEditCommand extends EditCommand {

	@Override
	public void performEdit(EditContext context, NinePatchImage npi) throws Exception {
		File specFile=new File(npi.source.toString() + ".spec");
		if (!specFile.exists()) {
			System.err.println("  - No spec file found " + specFile);
		}
		
		Properties props=new Properties();
		FileInputStream in=new FileInputStream(specFile);
		try {
			props.load(in);
		} finally {
			in.close();
		}
		
		// Build commands
		List<EditCommand> commands=new ArrayList<EditCommand>();
		String spec;
		
		spec=props.getProperty("sx", "").trim();
		if (!spec.isEmpty()) {
			commands.add(new SetScaleXEditCommand().parse(spec));
		}
		
		spec=props.getProperty("sy", "").trim();
		if (!spec.isEmpty()) {
			commands.add(new SetScaleYEditCommand().parse(spec));
		}
		
		spec=props.getProperty("px", "").trim();
		if (!spec.isEmpty()) {
			commands.add(new SetPaddingXEditCommand().parse(spec));
		}
		
		spec=props.getProperty("py", "").trim();
		if (!spec.isEmpty()) {
			commands.add(new SetPaddingYEditCommand().parse(spec));
		}
		
		// Run them
		context.applyCommand(npi, new ClearEditCommand());
		for (EditCommand cmd: commands) {
			context.applyCommand(npi, cmd);
		}
	}
	
	@Override
	public String toString() {
		return "Apply properties from .spec file";
	}

}
