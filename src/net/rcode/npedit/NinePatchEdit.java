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
public class NinePatchEdit {
	List<File> files=new ArrayList<File>();
	List<EditCommand> commands=new ArrayList<EditCommand>();
	File outputDirectory=new File("out");
	
	public NinePatchEdit() {
	}
	
	public void log(String msg) {
		System.err.println(msg);
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
		int w=template.image.getWidth();
		int h=template.image.getHeight();
		
		// X
		SetScaleXEditCommand xScale=new SetScaleXEditCommand();
		SetPaddingXEditCommand xPadding=new SetPaddingXEditCommand();
		for (int x=0; x<w; x++) {
			int pixel=template.image.getRGB(x, 0);
			if (!NinePatchImage.validatePixel(pixel)) {
				invalidPixel(fileName, x, 0);
			}
			
			if (pixel!=0) xScale.add(x);
			
			pixel=template.image.getRGB(x, h-1);
			if (!NinePatchImage.validatePixel(pixel)) {
				invalidPixel(fileName, x, h-1);
			}
			
			if (pixel!=0) xPadding.add(x);
		}
		if (xScale.hasRanges()) commands.add(xScale);
		if (xPadding.hasRanges()) commands.add(xPadding);
		
		// Y
		SetScaleYEditCommand yScale=new SetScaleYEditCommand();
		SetPaddingYEditCommand yPadding=new SetPaddingYEditCommand();
		for (int y=0; y<h; y++) {
			int pixel=template.image.getRGB(0, y);
			if (!NinePatchImage.validatePixel(pixel)) {
				invalidPixel(fileName, 0, y);
			}
			if (pixel!=0) yScale.add(y);
			
			pixel=template.image.getRGB(w-1, y);
			if (!NinePatchImage.validatePixel(pixel)) {
				invalidPixel(fileName, w-1, y);
			}
			if (pixel!=0) yPadding.add(y);
		}
		if (yScale.hasRanges()) commands.add(yScale);
		if (yPadding.hasRanges()) commands.add(yPadding);
	}

	protected void invalidPixel(String fileName, int x, int y) {
		log("WARNING: Invalid 9patch border pixel in " + fileName + " at (" + x + "," + y + ")");
	}
	
	public void processFile(File file) throws Exception {
		log("--> Processing file " + file);
		NinePatchImage npi=NinePatchImage.load(file);
		log("  - Image is " + npi.image.getWidth() + "x" + npi.image.getHeight() + " pixels");
		for (EditCommand cmd: commands) {
			log("  + " + cmd);
			cmd.performEdit(npi);
		}
		
		File outputFile=npi.getOutputFile();
		/*
		if (outputDirectory!=null) {
			outputFile=new File(outputDirectory, file.getName());
			outputFile.getParentFile().mkdirs();
		} else {
			outputFile=file;
		}
		*/
		
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
