package application;

import java.io.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.sun.javafx.geom.Vec3f;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import utilities.Utilities;

public class Controller {
	@FXML
	private Button selectFile;
	@FXML
	private Button STI_copy_col;
	@FXML
	private Button STI_copy_row;
	@FXML
	private Button STI_hist_row;
	@FXML
	private Button STI_hist_col;
	@FXML
	private Text file_selected;
	@FXML
	private ImageView imageView;
	
	String fileName = "test(default).mp4";
	private VideoCapture capture = null;
	private ScheduledExecutorService timer;
	private int numOfFrame;
	private ArrayList<Mat> newFrame_row;
	private ArrayList<Mat> newFrame_col;
	private ArrayList<Mat> frames = new ArrayList<>();
	private Mat ViewFrame_col = new Mat();
	private Mat ViewFrame_row = new Mat();
	private int size;
	
	@FXML
	public void getFileName(ActionEvent event) throws InterruptedException { // when click on "select file"
		FileChooser fc = new FileChooser();
		File selectedFile = fc.showOpenDialog(null);
		if(selectedFile != null) {
			fileName = selectedFile.getAbsolutePath();
			System.out.println("File Selection Worked");
		}
		else
			System.out.println("File Selection Failed");
		String[] name = fileName.split(Pattern.quote("\\"));
		file_selected.setText("File Name: " + name[name.length - 1]);
	}
	
	//=====================================================================================================================================
	
	@FXML
	public void STI_by_copy_col_pixel(ActionEvent event) throws InterruptedException{ // when click on "STI by copy column pixels"
		capture = new VideoCapture(fileName);
		numOfFrame = 0;
		if (capture.isOpened()) {
			  if (capture != null && capture.isOpened()) {
				    double framePerSecond = capture.get(Videoio.CAP_PROP_FPS);
				    size = (int) (capture.get(Videoio.CAP_PROP_FRAME_COUNT));
				    newFrame_col = new ArrayList<Mat>();
				    Runnable frameGrabber = new Runnable() {
				    	@Override
					    public void run() {
				    		Mat frame = new Mat();
					        if (capture.read(frame)) {
					        	double currentFrameNumber = capture.get(Videoio.CAP_PROP_POS_FRAMES);
					        	double totalFrameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
					        	if(currentFrameNumber == totalFrameCount) {
					        		capture.release();
					        	}
					        	Mat resizedImage = new Mat();
					        	Imgproc.resize(frame, resizedImage, new Size(32, 32));
					        	newFrame_col.add(resizedImage.col(16)); // grab each center column in each frame
					        	frames.add(frame);
					        } else {
					        	if (timer != null && !timer.isShutdown()) {
							    	timer.shutdown();
							    	try {
										timer.awaitTermination(Math.round(1000/framePerSecond), TimeUnit.MILLISECONDS);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							    	size = numOfFrame;
							    	ViewFrame_col.create(32, size, CvType.CV_8UC3);
							    	for (int i = 0; i < size; i++) {
							    		newFrame_col.get(i).copyTo(ViewFrame_col.col(i));
									}
							    	displayimage(ViewFrame_col); // display the image
							    }
					        }
					        numOfFrame++;
					    }
				    };
				    // run the frame grabber
				    timer = Executors.newSingleThreadScheduledExecutor();
				    timer.scheduleAtFixedRate(frameGrabber, 0, Math.round(1000/framePerSecond), TimeUnit.MILLISECONDS);
			  }
		}
	}
	
	//=====================================================================================================================================
	
	@FXML
	public void STI_by_copy_row_pixel(ActionEvent event) throws InterruptedException{ // when click on "STI by copy row pixels"
		capture = new VideoCapture(fileName);
		numOfFrame = 0;
		if (capture.isOpened()) {
			  if (capture != null && capture.isOpened()) {
				    double framePerSecond = capture.get(Videoio.CAP_PROP_FPS);
				    size = (int) (capture.get(Videoio.CAP_PROP_FRAME_COUNT));
				    newFrame_row = new ArrayList<Mat>();
				    Runnable frameGrabber = new Runnable() {
				    	@Override
					    public void run() {
				    		Mat frame = new Mat();
					        if (capture.read(frame)) { // read in a frame
					        	double currentFrameNumber = capture.get(Videoio.CAP_PROP_POS_FRAMES);
					        	double totalFrameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
					        	if(currentFrameNumber == totalFrameCount) {
					        		capture.release();
					        	}
					        	Mat resizedImage = new Mat();
					        	Imgproc.resize(frame, resizedImage, new Size(32, 32));
					        	newFrame_row.add(resizedImage.row(16)); // grab each center row from each frame
					        } else {
					        	if (timer != null && !timer.isShutdown()) {
							    	timer.shutdown();
							    	try {
										timer.awaitTermination(Math.round(1000/framePerSecond), TimeUnit.MILLISECONDS);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							    	size = numOfFrame;
							    	ViewFrame_row.create(size, 32, CvType.CV_8UC3);
							    	for (int i = 0; i < size; i++) {
							    		newFrame_row.get(i).copyTo(ViewFrame_row.row(i));
									}
							    	displayimage(ViewFrame_row); // display image (made by center rows)
							    }
					        }
					        numOfFrame++;
					    }
				    	
				    };
				    // run the frame grabber
				    timer = Executors.newSingleThreadScheduledExecutor();
				    timer.scheduleAtFixedRate(frameGrabber, 0, Math.round(1000/framePerSecond), TimeUnit.MILLISECONDS);
			  }
		}
	}
	
	//=====================================================================================================================================
	
	@FXML	
	public void STI_by_col_hist_diff(ActionEvent event) throws InterruptedException{ // when click on "STI by histogram difference (column)"
		ArrayList<ArrayList<Double>> I_total = new ArrayList<ArrayList<Double>>();
		
		for(int col_number = 0; col_number < 32; col_number++) { // for each column
			ArrayList<Mat> same_col = new ArrayList<>();
			if(frames.size() == 0)
				backupFrameGrabber();
			for(int i = 0; i < frames.size(); i++) { // in every frame
				Mat resizedImage = new Mat();
	        	Imgproc.resize(frames.get(i), resizedImage, new Size(32, 32));
				same_col.add(resizedImage.col(col_number)); // same_col is made by same location columns in every frame
			}
			ArrayList<Mat> chroma = cvtcols2Chroma(same_col); // convert to chromaticity
			
			ArrayList<Double[][]> histogram = new ArrayList<>();
			// scale the r and g for every pixel in every column and count
			for(int i = 0; i < chroma.size(); i++) {
				Double[][] counter = new Double[6][6];
				for(int a = 0; a < 6; a++)
					for(int b = 0; b < 6; b++)
						counter[a][b] = 0.0;
				for(int row = 0; row < chroma.get(i).rows(); row++) {
					double[] intensity = chroma.get(i).get(row, 0);
					double R = intensity[0] / 255 * 6;
					double G = intensity[1] / 255 * 6;
					int index_R = (int)Math.floor(R);
					int index_G = (int)Math.floor(G);
					if(index_R >= 6)
						index_R = 5;
					if(index_G >= 6)
						index_G = 5;
					counter[index_R][index_G] += 1;
				}
				histogram.add(counter);
			}
			
			ArrayList<Double> I = new ArrayList<>();
			for(int i = 0; i < histogram.size() - 1; i++) {
				Double[][] H_previous = normalize(histogram.get(i));
				Double[][] H_this = normalize(histogram.get(i+1));
				double total = 0;
				for(int row = 0; row < 6; row++) {
					for(int col = 0; col < 6; col++) {
						total += Math.min(H_this[row][col], H_previous[row][col]); // do histogram intersection
					}
				}
				I.add(total);
			}
			I_total.add(I);
		}
		// every row in STI image should be made from every row from I_total, where I_total is ArrayList<ArrayList<Double>> type
		Mat view_image = new Mat();
		view_image.create(I_total.size(), I_total.get(0).size(), CvType.CV_8UC3); // view_image = 32 * 147 for default video
		for(int i = 0; i < I_total.size(); i++) { // 32
			for(int j = 0; j < I_total.get(i).size(); j++) { // 147
				double[] data = {I_total.get(i).get(j) *255 ,I_total.get(i).get(j) *255 ,I_total.get(i).get(j)*255};
				view_image.put(i, j, data);
			}
		}
		displayimage(view_image);
	}
	
	//=====================================================================================================================================
	
	@FXML
	public void STI_by_row_hist_diff(ActionEvent event) throws InterruptedException{ // when click on "STI by histogram difference(row)"
		ArrayList<ArrayList<Double>> I_total = new ArrayList<ArrayList<Double>>();
		
		for(int row_number = 0; row_number < 32; row_number++) {
			ArrayList<Mat> same_row = new ArrayList<>();
			if(frames.size() == 0)
				backupFrameGrabber();
			for(int i = 0; i < frames.size(); i++) {
				Mat resizedImage = new Mat();
	        	Imgproc.resize(frames.get(i), resizedImage, new Size(32, 32));
				same_row.add(resizedImage.row(row_number)); // same_row is made by same location rows in every frame
			}
			ArrayList<Mat> chroma = cvtrows2Chroma(same_row); // change into r, g
			
			ArrayList<Double[][]> histogram = new ArrayList<>(); // increase count
			for(int i = 0; i < chroma.size(); i++) {
				Double[][] counter = new Double[6][6];
				for(int a = 0; a < 6; a++)
					for(int b = 0; b < 6; b++)
						counter[a][b] = 0.0;
				for(int col = 0; col < chroma.get(i).cols(); col++) {
					double[] intensity = chroma.get(i).get(0, col);
					double R = intensity[0] / 255 * 6;
					double G = intensity[1] / 255 * 6;
//					System.out.println("R = " + R + " ; G = " + G);
					int index_R = (int)Math.floor(R);
					int index_G = (int)Math.floor(G);
					if(index_R >= 6)
						index_R = 5;
					if(index_G >= 6)
						index_G = 5;
					counter[index_R][index_G] += 1;
				}
				histogram.add(counter);
			}
			
			ArrayList<Double> I = new ArrayList<>();
			for(int i = 0; i < histogram.size() - 1; i++) {
				Double[][] H_previous = normalize(histogram.get(i));
				Double[][] H_this = normalize(histogram.get(i+1));
				double total = 0;
				for(int row = 0; row < 6; row++) {
					for(int col = 0; col < 6; col++) {
						total += Math.min(H_this[row][col], H_previous[row][col]); // apply histogram intersection
					}
				}
				I.add(total);
			}
			I_total.add(I);
		}
		
		
		// every row in STI image should be made from every row from I
		Mat view_image = new Mat();
		view_image.create(I_total.size(), I_total.get(0).size(), CvType.CV_8UC3); // view_image = 32 * 147
		for(int i = 0; i < I_total.size(); i++) { // 32
			for(int j = 0; j < I_total.get(i).size(); j++) { // 147
				double[] data = {I_total.get(i).get(j) *255 ,I_total.get(i).get(j) *255 ,I_total.get(i).get(j)*255};
				view_image.put(i, j, data);
			}
		}
		displayimage(view_image);
	}
	
	//=====================================================================================================================================
	//=====================================================================================================================================
	//=========================================		SUPPORTING FUNCTIONS	===============================================================
	//=====================================================================================================================================
	//=====================================================================================================================================
	
	private ArrayList<Mat> cvtcols2Chroma(ArrayList<Mat> col_Frame) { // convert all columns grabbed in each frame from rgb to rg
		ArrayList<Mat> temp = new ArrayList<>();
		for(int i = 0; i < col_Frame.size(); i++) {
			Mat temp_column = new Mat();
			temp_column.create(col_Frame.get(i).rows(), 1, CvType.CV_8UC3);
			for(int row = 0; row < col_Frame.get(i).rows(); row++) {
				double[] intensity = col_Frame.get(i).get(row, 0);
				double R = intensity[0];
				double G = intensity[1];
				double B = intensity[2];
//				System.out.println("R = " + R + " ; G = " + G + " ; B = " + B);
				double total = R + G + B;
				double chroma_R;
				double chroma_G;
				if(total == 0) {
					chroma_R = 0;
					chroma_G = 0;
				} else {
					chroma_R = R/total;
					chroma_G = G/total;
				}
				double[] new_intensity = {chroma_R * 255, chroma_G * 255, B};
//				System.out.println(chroma_R);
				temp_column.put(row, 0, new_intensity); // potential error this line
			}
			temp.add(temp_column);
		}
		return temp;
	}
	
	private ArrayList<Mat> cvtrows2Chroma(ArrayList<Mat> row_Frame) { // convert all rows grabbed in each frame from rgb to rg
		ArrayList<Mat> temp = new ArrayList<>();
		for(int i = 0; i < row_Frame.size(); i++) {
			Mat temp_row = new Mat();
			temp_row.create(1, row_Frame.get(i).cols(), CvType.CV_8UC3);
			for(int col = 0; col < row_Frame.get(i).cols(); col++) {
				double[] intensity = row_Frame.get(i).get(0, col);
				double R = intensity[0];
				double G = intensity[1];
				double B = intensity[2];
//				System.out.println("R = " + R + " ; G = " + G + " ; B = " + B);
				double total = R + G + B;
				double chroma_R;
				double chroma_G;
				if(total == 0) {
					chroma_R = 0;
					chroma_G = 0;
				} else {
					chroma_R = R/total;
					chroma_G = G/total;
				}
				double[] new_intensity = {chroma_R * 255, chroma_G * 255, B};
//				System.out.println(chroma_R);
				temp_row.put(0, col, new_intensity);
			}
			temp.add(temp_row);
		}
//		for(int i = 0; i < temp.size(); i++) {
//			for(int row = 0; row < temp.get(i).rows(); row++) {
//				double[] intensity = temp.get(i).get(row, 0);
//				double R = intensity[0];
//				double G = intensity[1];
//				double B = intensity[2];
////				System.out.println("R = " + R + " ; G = " + G + " ; B = " + B);
//			}
//		}
		return temp;
	}
	
	private void backupFrameGrabber() {
		capture = new VideoCapture(fileName);
		numOfFrame = 0;
		if (capture.isOpened()) {
			  if (capture != null && capture.isOpened()) {
				    double framePerSecond = capture.get(Videoio.CAP_PROP_FPS);
				    size = (int) (capture.get(Videoio.CAP_PROP_FRAME_COUNT));
				    newFrame_row = new ArrayList<Mat>();
				    Runnable frameGrabber = new Runnable() {
				    	@Override
					    public void run() {
				    		Mat frame = new Mat();
					        if (capture.read(frame)) {
					        	double currentFrameNumber = capture.get(Videoio.CAP_PROP_POS_FRAMES);
					        	double totalFrameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
					        	if(currentFrameNumber == totalFrameCount) {
					        		capture.release();
					        	}
					        	frames.add(frame);
					        } else {
					        	if (timer != null && !timer.isShutdown()) {
							    	timer.shutdown();
							    	try {
										timer.awaitTermination(Math.round(1000/framePerSecond), TimeUnit.MILLISECONDS);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							    }
					        }
					        numOfFrame++;
					    }
				    };
				    // run the frame grabber
				    timer = Executors.newSingleThreadScheduledExecutor();
				    timer.scheduleAtFixedRate(frameGrabber, 0, Math.round(1000/framePerSecond), TimeUnit.MILLISECONDS);
			  }
		}
	}
	
	private Double[][] normalize(Double[][] doubles){
		double sum = 0;
		Double[][] temp = doubles;
		for(int i = 0; i < 6; i++)
			for(int j = 0; j < 6; j++)
				sum += doubles[i][j];
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				temp[i][j] = doubles[i][j] / sum;
			}
		}
		return temp;
	}
	
	private void displayimage(Mat frame) { // supporting function to display mat format image
		if(frame.empty()) {
			System.out.println("empty image");
		} else {
			Image img = Utilities.mat2Image(frame);
			imageView.setImage(img);
		}
	}
}
