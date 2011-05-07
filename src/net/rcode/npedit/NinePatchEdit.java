package net.rcode.npedit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Edit nine patch images from the command line
 * @author stella
 *
 */
public class NinePatchEdit {
	List<File> files=new ArrayList<File>();
	List<EditCommand> commands=new ArrayList<EditCommand>();
	File outputDirectory=new File("out");
	
	public NinePatchEdit() {
	}
	
	public void log(String msg) {
		System.err.println(msg);
	}
	
	private String nextArg(String[] args, int index) {
		if (index>=args.length) {
			log("Argument " + args[index-1] + " expects an operand.");
			System.exit(2);
		}
		return args[index];
	}
	
	public void initFromArgs(String[] args) {
		boolean overwrite=false;
		for (int i=0; i<args.length; i++) {
			String arg=args[i];
			if (!arg.startsWith("-")) {
				// Its just a file
				files.add(new File(arg));
			} else {
				if ("-clear".equals(arg)) {
					commands.add(new ClearEditCommand());
				} else if ("-out".equals(arg)) {
					outputDirectory=new File(nextArg(args, ++i));
					log("Will save images to " + outputDirectory);
				} else if ("-overwrite".equals(arg)) {
					overwrite=true;
				} else if ("-sx".equals(arg)) {
					commands.add(new SetScaleXEditCommand().parse(nextArg(args, ++i)));
				} else if ("-sy".equals(arg)) {
					commands.add(new SetScaleYEditCommand().parse(nextArg(args, ++i)));
				} else if ("-px".equals(arg)) {
					commands.add(new SetPaddingXEditCommand().parse(nextArg(args, ++i)));
				} else if ("-py".equals(arg)) {
					commands.add(new SetPaddingYEditCommand().parse(nextArg(args, ++i)));
				} else {
					log("Unrecognized command line option: " + arg);
					System.exit(1);
				}
			}
		}
		
		if (commands.size()==0) {
			log("No commands specified.  Not doing anything!");
			System.exit(1);
		}
		
		if (!overwrite && outputDirectory==null) {
			log("Cowardly refusing to overwrite source images.  Specify -overwrite or -out <dir>");
			System.exit(1);
		}
	}
	
	public void processFile(File file) throws Exception {
		log("--> Processing file " + file);
		NinePatchImage npi=NinePatchImage.load(file);
		log("  - Image is " + npi.image.getWidth() + "x" + npi.image.getHeight() + " pixels");
		for (EditCommand cmd: commands) {
			log("  + " + cmd);
			cmd.performEdit(npi);
		}
		
		File outputFile;
		if (outputDirectory!=null) {
			outputFile=new File(outputDirectory, file.getName());
			outputFile.getParentFile().mkdirs();
		} else {
			outputFile=file;
		}
		
		log("  => Saving to " + outputFile);
		npi.save(outputFile);
	}
	
	public void processFiles() throws Exception {
		for (File file: files) {
			processFile(file);
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.setProperty("java.awt.headless", "true");
		NinePatchEdit npe=new NinePatchEdit();
		npe.initFromArgs(args);
		
		npe.processFiles();
	}
}
