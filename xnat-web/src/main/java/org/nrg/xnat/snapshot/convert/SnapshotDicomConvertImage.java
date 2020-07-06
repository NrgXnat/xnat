package org.nrg.xnat.snapshot.convert;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xnat.plexiviewer.lite.io.PlexiFileSaver;
import org.nrg.xnat.plexiviewer.utils.FileUtils;
import org.nrg.xnat.plexiviewer.utils.ImageUtils;
import org.nrg.xnat.plexiviewer.utils.UnzipFile;
import org.nrg.xnat.plexiviewer.utils.transform.BitConverter;
import org.nrg.xnat.plexiviewer.utils.transform.IntensitySetter;
import org.nrg.xnat.plexiviewer.utils.transform.PlexiMontageMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Toolbar;
import ij.io.FileInfo;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

/**
 * @author pradeep.d
 *
 */
public class SnapshotDicomConvertImage {

	private int n, start;
	private FileInfo fi;
	private String info1;
	private String directory;
	private String[] list;
	private String title;
	private boolean zipped = false;
	private int width = 0, height = 0;

	/**
	 * @param dir
	 */
	public SnapshotDicomConvertImage(String dir) {
		directory = dir;
		list = (new File(directory)).list();
		String zext = ".gz";
		for (int i = 0; i < list.length; i++) {
			if (list[i].endsWith(zext)) {
				zipped = true;
			}
		}
		unzip();
	}

	/**
	 * 
	 */
	private void unzip() {
		if (zipped) {
			String suffix = "_" + new Random().nextInt();
			File tempDir = new File(FileUtils.getTempFolder());
			try {
				File dir = File.createTempFile("NRG", suffix, tempDir);
				if (dir.exists()) {
					dir.delete();
				}
				boolean success = dir.mkdir();
				for (int i = 0; i < list.length; i++) {
					new UnzipFile().gunzip(directory + File.separator + list[i], dir.getPath());
				}
				directory = dir.getPath();
				list = (new File(directory)).list();
			} catch (IOException ioe) {
				_log.error("DicomSequence:: Unable to create temporary directory " + ioe.getMessage());
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}

	/**
	 * @return
	 */
	public ImagePlus getImagePlus() {
		ImagePlus imagesPlus = null;
		n = list.length;
		ImageStack stack = null;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		Calibration cal = null;
		boolean allSameCalibration = true;
		int count = 0;

		try {
			String dimResult = getResizeDimensionCalc();
			Integer resizeWid = null;
			Integer resizeHeight = null;
			if(dimResult != null) {
			   String reSize[]  =	dimResult.split("X");
			   resizeWid = Integer.parseInt(reSize[0]);
			   resizeHeight = Integer.parseInt(reSize[1]);
			   _log.debug("Different dimension is found " +dimResult);
			}
			
			for (int i = start; i < list.length; i++) {
				Opener opener = new Opener();
				opener.setSilentMode(true);
				ImagePlus imp = opener.openImage(directory, list[i]);
				if (imp != null && stack == null) {
					width = imp.getWidth();
					height = imp.getHeight();
					cal = imp.getCalibration();
					ColorModel cm = imp.getProcessor().getColorModel();
					if(resizeWid != null & resizeHeight != null) {
						stack = new ImageStack(resizeWid, resizeHeight, cm);
						width = resizeWid; height = resizeHeight;
					} else {
						stack = new ImageStack(width, height, cm);
					}
				}

				if (imp == null) {
					if (!list[i].startsWith(".")) {
						_log.error(list[i] + ": unable to open");
					}
					continue;
				}
                
				if(imp.getWidth() != width || imp.getHeight() != height) {
					_log.error(list[i] + ": wrong size; " + width + "x" + height + " expected, " + imp.getWidth() + "x" + imp.getHeight() + " found");
					double tempWidth = 0, tempHeight = 0 ;
					if(imp.getWidth() > resizeWid && resizeWid > 0) {
						tempWidth = resizeWid;
						tempHeight = (double)(imp.getHeight()*resizeWid)/(double)imp.getWidth();
						if(tempHeight > resizeHeight && tempHeight > 0 ) {
					    	tempHeight = resizeHeight;
					    	tempWidth = (double)(tempWidth*resizeHeight)/(double)tempHeight;
					    }
                    }
					if(imp.getHeight() > resizeHeight && resizeHeight >0) {
						tempHeight = resizeHeight;
						tempWidth = (double)(imp.getWidth()*resizeHeight)/(double)imp.getHeight();
						if(tempWidth > resizeWid && tempWidth > 0) {
							tempWidth = resizeWid ;
							tempHeight = (double)(tempHeight*resizeWid)/(double)tempWidth;
						}
					}
					if((int)Math.round(tempWidth) >0  && (int)Math.round(tempHeight) >0) {
						 ImageProcessor imageProcessor = imp.getProcessor();
						 imageProcessor = imageProcessor.resize((int)Math.round(tempWidth), (int)Math.round(tempHeight));
		                 imp.setProcessor(imageProcessor);
		                 _log.error("resize Width :: "+tempWidth  + "  --resize Height:: "+tempHeight );
					}
				}
				
				ImageStack inputStack = imp.getStack();
				if(imp.getWidth() != width || imp.getHeight() != height) {
					int xCenter = 0,yCenter=0 ;
					if(resizeWid>imp.getWidth()) {
						xCenter = (resizeWid - imp.getWidth())/2;
					}
					if(resizeHeight>imp.getHeight()) {
						yCenter = (resizeHeight - imp.getHeight())/2;
					}
					inputStack = resizeStack(inputStack, resizeWid, resizeHeight, xCenter, yCenter);
				}
				for (int slice = 1; slice <= inputStack.getSize(); slice++) {
					ImageProcessor ip = inputStack.getProcessor(slice);
					if (slice == 1) {
						count++;
					}
					if (ip.getMin() < min) {
						min = ip.getMin();
					}
					if (ip.getMax() > max) {
						max = ip.getMax();
					}
					stack.addSlice(ip);
				}
				if (count >= n) {
					break;
				}
			}
			if (stack != null && stack.getSize() > 0) {
				ImagePlus imagePlus = new ImagePlus(title, stack);
				if (imagePlus.getType() == ImagePlus.GRAY16 || imagePlus.getType() == ImagePlus.GRAY32) {
					imagePlus.getProcessor().setMinAndMax(min, max);
				}
				imagePlus.setFileInfo(fi);
				if (allSameCalibration) {
					imagePlus.setCalibration(cal);
				}
				if (imagePlus.getStackSize() == 1 && info1 != null) {
					imagePlus.setProperty("Info", info1);
				}
				imagesPlus = imagePlus;
			}
		} catch (OutOfMemoryError e) {

		} finally {
			if (zipped) {
				FileUtils.deleteFile(directory, true);
			}
		}
		return imagesPlus;
	}

	/**
	 * @param scan
	 * @param cachepaths
	 * @param montageFlag
	 * @param gridview
	 * @return
	 * @throws Exception
	 */
	public File createSnapshotImage(XnatImagescandataBean scan, String cachepaths, boolean montageFlag, String gridview)
			throws Exception {
		ImagePlus baseimage = getImagePlus();
		File targetFile = null;
		ImagePlus snapshot = getSnapshot(baseimage, montageFlag, gridview);

		if (snapshot != null) {
			BitConverter converter = new BitConverter();
			converter.convertTo8BitColor(snapshot);
			String tbfilenameroot = scan.getImageSessionId() + "_" + scan.getId() ;
			if(!gridview.isEmpty()) {
				tbfilenameroot = tbfilenameroot+ "_" + gridview.toUpperCase();
			} 
			PlexiFileSaver fs = new PlexiFileSaver(snapshot.getImage());
			String fileName = tbfilenameroot + ".gif";
			String filePath = cachepaths + File.separator + fileName;
			boolean saved = fs.saveImageAsGif(filePath);
			if (!saved) {
				throw new Exception(
						"Couldnt save file snapshot for scan " + scan.getId() + " at the location " + filePath);
			}
			targetFile = new File(filePath);
		}
		return targetFile;
	}


	/**
	 * @param baseimage
	 * @param montage
	 * @param gridview
	 * @return
	 * @throws Exception
	 */
	private ImagePlus getSnapshot(ImagePlus baseimage, boolean montage, String gridview) throws Exception {
		ImagePlus rtn = null;
		if (montage) {
			rtn = createMontage(baseimage, gridview);
		} else {
			if (baseimage != null) {
				int sliceNo = 5;
				if (baseimage.getStackSize() == 1) {
					sliceNo = 1;
				} else if (baseimage.getStackSize() < sliceNo) {
					sliceNo = 2;
				}
				baseimage.setSlice(sliceNo);
				baseimage.updateImage();
				baseimage.getProcessor().setColor(Color.WHITE);
				baseimage.getProcessor().setFont(new Font("Serif", Font.BOLD, 10));
				baseimage.getProcessor().drawString("Frame: " + sliceNo, baseimage.getWidth(),
						baseimage.getHeight());
				baseimage.updateImage();
				rtn = baseimage;
			}
		}
		return rtn;
	}

	/**
	 * @param image
	 * @param gridViews
	 * @return
	 * @throws Exception
	 */
	private ImagePlus createMontage(ImagePlus image, String gridViews) throws Exception {
		int columns = 1;
		int rows = 1;
		PlexiMontageMaker mm = new PlexiMontageMaker();
		try {
			if (gridViews != null && !gridViews.isEmpty()) {
				String[] rowCol = gridViews.toUpperCase().split("X");
				rows = Integer.parseInt(rowCol[0]);
				columns = Integer.parseInt(rowCol[1]);
			}
		} catch (NumberFormatException ex) {
			_log.error("Error createMontage :: " + ex.getMessage());
			throw new NumberFormatException("Provide valid Grid views ROWXCOL paramter");

		} catch (Exception ex) {
			_log.error("Error createMontage :: " + ex.getMessage());
			throw new Exception("Provide valid Grid views ROWXCOL paramter");
		} 
		
		if(image.getStackSize() <= 0) {
			_log.error("Error createMontage :: DICOM Image does not exist ");
			throw new Exception("DICOM Image does not exist");
		}
		Hashtable<?, ?> attribs = ImageUtils.getSliceIncrement(image, columns * rows);

		int startslice = ((Integer) attribs.get("startslice")).intValue();
		int endslice = 0;
		if (rows == 1 && columns == 1) {
			endslice = image.getStackSize() ;
		} else {
			endslice = ((Integer) attribs.get("endslice")).intValue();
		}
		int increment = ((Integer) attribs.get("increment")).intValue();
		IntensitySetter is = new IntensitySetter(image, true);
		is.autoAdjust(image, image.getProcessor());

		image = mm.makeMontage(image, columns, rows, 1.0, startslice, endslice, increment, true, false);
		image.getProcessor().resetMinAndMax();
		return image;
	}

	/**
	 * @param file
	 */
	public void deleteFile(File file) {
		if (file != null && file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * @return
	 */
	private String getResizeDimensionCalc() {
		Map<String, Integer> dimList = new HashMap<String, Integer>();
		String  dimResult = null;
		for (int j = start; j < list.length; j++) {
			Opener opener = new Opener();
			opener.setSilentMode(true);
			ImagePlus impagePlug = opener.openImage(directory, list[j]);
			Integer maxCount = 0;
			if (impagePlug != null ) {
				String dimension = impagePlug.getWidth()+ "X" +impagePlug.getHeight();
				Integer dimCount = dimList.get(dimension);
				dimList.put(dimension, (dimCount == null) ? 1 : dimCount + 1);
				if(dimList.size()>1) {
					if(dimList.get(dimension) >  maxCount) {
						maxCount = dimList.get(dimension);
						dimResult = dimension;
					}
				}
			}
		}
		return dimResult;
	}
	
	/**
	 * @param stackOld
	 * @param widthNew
	 * @param heightNew
	 * @param xOff
	 * @param yOff
	 * @return
	 */
	private ImageStack resizeStack(ImageStack stackOld, int widthNew, int heightNew, int xOff, int yOff) {
		int nFrames = stackOld.getSize();
		ImageProcessor imProcOld = stackOld.getProcessor(1);
		Color colorBack = Toolbar.getBackgroundColor();
		ImageStack stackNew = new ImageStack(widthNew, heightNew, stackOld.getColorModel());
		ImageProcessor imProcNew;
		for (int i=1; i<=nFrames; i++) {
			IJ.showProgress((double)i/nFrames);
			imProcNew = imProcOld.createProcessor(widthNew, heightNew);
			imProcNew.setColor(colorBack);
			imProcNew.fill();
			imProcNew.insert(stackOld.getProcessor(i), xOff, yOff);
			stackNew.addSlice(null, imProcNew);
		}
		return stackNew;
	}

	private static final Logger _log = LoggerFactory.getLogger(SnapshotDicomConvertImage.class);
}
