package net.rcode.npedit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Edit nine patch images from the command line
 * @author stella
 *
 */
public class NinePatchEdit implements EditContext {
	List<File> files=new ArrayList<File>();
	List<EditCommand> commands=new ArrayList<EditCommand>();
	File outputDirectory=null;
	
	public NinePatchEdit() {
	}
	
	public void log(String msg) {
		System.err.println(msg);
	}
	
	@Override
	public void logCommandDetail(String msg) {
		log("  - " + msg);
	}
	
	public void usage() throws IOException {
		InputStream in=getClass().getResourceAsStream("usage.txt");
		byte[] buffer=new byte[1024];
		for (;;) {
			int r=in.read(buffer);
			if (r<0) break;
			System.err.write(buffer, 0, r);
		}
		in.close();
	}
	
	public void syntax(String error) throws IOException {
		System.err.println("Syntax error: " + error);
		usage();
		System.exit(2);
	}
	
	private String nextArg(String[] args, int index) throws IOException {
		if (index>=args.length) {
			syntax("Argument " + args[index-1] + " expects an operand.");
		}
		return args[index];
	}
	
	public void initFromArgs(String[] args) throws IOException {
		if (args.length==0) {
			syntax("Expected arguments");
		}
		
		//boolean overwrite=false;
		for (int i=0; i<args.length; i++) {
			String arg=args[i];
			if (!arg.startsWith("-")) {
				// Its just a file
				files.add(new File(arg));
			} else {
				if ("-clear".equals(arg)) {
					commands.add(new ClearEditCommand());
				} else if ("-strip".equals(arg)) {
					commands.add(new StripEditCommand());
				} else if ("-out".equals(arg)) {
					outputDirectory=new File(nextArg(args, ++i));
					log("Will save images to " + outputDirectory);
				//} else if ("-overwrite".equals(arg)) {
				//	overwrite=true;
				} else if ("-template".equals(arg)) {
					commandsFromTemplate(nextArg(args, ++i));
				} else if ("-sx".equals(arg)) {
					commands.add(new SetScaleXEditCommand().parse(nextArg(args, ++i)));
				} else if ("-sy".equals(arg)) {
					commands.add(new SetScaleYEditCommand().parse(nextArg(args, ++i)));
				} else if ("-px".equals(arg)) {
					commands.add(new SetPaddingXEditCommand().parse(nextArg(args, ++i)));
				} else if ("-py".equals(arg)) {
					commands.add(new SetPaddingYEditCommand().parse(nextArg(args, ++i)));
				} else if ("-expand".equals(arg)) {
					ExpandEditCommand cmd=new ExpandEditCommand();
					cmd.parse(nextArg(args, ++i));
					commands.add(cmd);
				} else if ("-spec".equals(arg)) {
					commands.add(new SpecEditCommand());
				} else {
					syntax("Unrecognized command line option: " + arg);
				}
			}
		}
		
		if (commands.size()==0) {
			syntax("No commands specified.  Not doing anything!");
		}
	}
	
	public void commandsFromTemplate(String fileName) throws IOException {
		NinePatchImage template=NinePatchImage.load(new File(fileName));
		template.ensureNinePatch();
		
		int[] sxmarkers=template.getMarkers(NinePatchImage.MODE_SCALEX);
		int[] symarkers=template.getMarkers(NinePatchImage.MODE_SCALEY);
		int[] pxmarkers=template.getMarkers(NinePatchImage.MODE_PADDINGX);
		int[] pymarkers=template.getMarkers(NinePatchImage.MODE_PADDINGY);
		
		if (sxmarkers.length>0) {
			SetScaleXEditCommand cmd=new SetScaleXEditCommand();
			cmd.add(sxmarkers);
			commands.add(cmd);
		}
		if (symarkers.length>0) {
			SetScaleYEditCommand cmd=new SetScaleYEditCommand();
			cmd.add(symarkers);
			commands.add(cmd);
		}
		if (pxmarkers.length>0) {
			SetPaddingXEditCommand cmd=new SetPaddingXEditCommand();
			cmd.add(pxmarkers);
			commands.add(cmd);
		}
		if (pymarkers.length>0) {
			SetPaddingYEditCommand cmd=new SetPaddingYEditCommand();
			cmd.add(pymarkers);
			commands.add(cmd);
		}
	}

	protected void invalidPixel(String fileName, int x, int y, int pixel) {
		log("WARNING: Invalid 9patch border pixel in " + fileName + " at (" + x + "," + y + "): #" + 
				Integer.toHexString(pixel));
	}
	
	public void processFile(File file) throws Exception {
		log("--> Processing file " + file);
		NinePatchImage npi=NinePatchImage.load(file);
		log("  - Image is " + npi.image.getWidth() + "x" + npi.image.getHeight() + " pixels");
		for (EditCommand cmd: commands) {
			applyCommand(npi, cmd);
		}
		
		File outputFile=npi.getOutputFile();
		if (outputDirectory!=null) {
			outputFile=new File(outputDirectory, outputFile.getName());
			outputFile.getParentFile().mkdirs();
		}
		
		log("  => Saving to " + outputFile);
		npi.save(outputFile);
	}

	@Override
	public void applyCommand(NinePatchImage npi, EditCommand cmd)
			throws Exception {
		log("  + " + cmd);
		cmd.performEdit(this, npi);
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
