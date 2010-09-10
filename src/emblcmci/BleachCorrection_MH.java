package emblcmci;
/**
 * based on "Match_To_Image_Histogram.java"
 * (http://www.imagingbook.com)
 * original package from above site "histogram2" is
 *  required for this plugin (already included in .jar).  
 * 
 *  works with 8bit and 16 bit stacks. 
 *  Kota Miura (CMCI, EMBL Heidelberg)
 */

import histogram2.HistogramMatcher;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class BleachCorrection_MH {// implements PlugIn { 

	
//	public void run(String arg) {
//		if (WindowManager.getCurrentImage()==null) return;
//		if (WindowManager.getCurrentImage().getStackSize()<2) {
//			IJ.showMessage("need a stack!");
//			return;
//		}
//		ImagePlus imp = new Duplicator().run(WindowManager.getCurrentImage());//, "bleach_corrected") ;
//
//		if (imp.getBitDepth()!= 8 && imp.getBitDepth()!=16){
//			IJ.showMessage("should be 8 or 16 bit image");
//			return;
//		}
//		bleachCorrectionHM(imp);
//	}
	
	public void bleachCorrectionHM(ImagePlus imp){
	
		int histbinnum = 0;
		if (imp.getBitDepth()==8) histbinnum = 256;
			else if (imp.getBitDepth()==16) histbinnum = 65536;//65535;
		
		boolean is3DT = false; 
		int zframes = 1;
		int timeframes = 1;
		int[] impdimA = imp.getDimensions();
		IJ.log("slices"+Integer.toString(impdimA[3])+"  -- frames"+Integer.toString(impdimA[4]));
		//IJ.log(Integer.toString(imp.getNChannels())+":"+Integer.toString(imp.getNSlices())+":"+ Integer.toString(imp.getNFrames()));
		if (impdimA[3]>1 && impdimA[4]>1){	// if slices and frames are both more than 1
			is3DT =true;
			zframes = impdimA[3];
			timeframes = impdimA[4];
			if ((zframes*timeframes) != imp.getStackSize()){
				IJ.showMessage("slice and time frames do not match with the length of the stack. Please correct!");
				return;
			}
		}
		
		ImageStack stack = imp.getStack();
		ImageProcessor ipA = null;
		ImageProcessor ipB = null;
		HistogramMatcher m = new HistogramMatcher();
		int[] hA = new int[histbinnum];
		int[] hB = new int[histbinnum];
		int[] F = new int[histbinnum];
		int[] histB = null;	//for each slice
		int[] histA = null;
		//IJ.log(Integer.toString(stack.getSize()));
		int i =0; 
		int j =0; 
		int k =0;
		/* in case of 3D, stack histogram of the first time point is measured, and then 
		 *  this stack histogram is used as reference (hB) for the rest of time points.  
		 */
		if (is3DT){
			//should implement here,
			for (i =0; i< timeframes; i++){
				if (i==0){
					for (j=0; j<zframes; j++){
						ipB = stack.getProcessor(i*zframes+j+1);
						histB = ipB.getHistogram();
						for (k=0; k<histbinnum; k++) hB[k] += histB[k];
					}
				} else {
					for (k=0; k<histbinnum; k++) hA[k] = 0;
					for (j=0; j<zframes; j++){
						ipA = stack.getProcessor(i*zframes+j+1);
						histA = ipA.getHistogram();
						for (k=0; k<histbinnum; k++) hA[k] += histA[k];						
					}					
					F = m.matchHistograms(hA, hB);
					for (j=0; j<zframes; j++){
						ipA = stack.getProcessor(i*zframes+j+1);
						ipA.applyTable(F);
					}
					IJ.log("corrected time point: "+Integer.toString(i+1));
				}
			}
				
		} else {		//2D case. 
			for (i=0; i<stack.getSize(); i++){
				if (i==0) {
					ipB = stack.getProcessor(i+1);
					hB = ipB.getHistogram();
				}
				else {
					ipA = stack.getProcessor(i+1);
					hA = ipA.getHistogram();
					F = m.matchHistograms(hA, hB);
					ipA.applyTable(F);
					IJ.log("corrected frame: "+Integer.toString(i+1));
				}
			}
		}
		imp.show();
	}

}
