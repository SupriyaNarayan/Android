package com.example.seamcarver3;

import java.io.File;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
	
public class SeamCarving3 {
	
	/**
	 * This method convert the color image into grayscale image
	 * @param BufferedImage -- > img
	 * @return BufferedImage  --> img
	 */
	public static Bitmap grayOut(Bitmap bmpOriginal) {
		int width, height;
	    height = bmpOriginal.getHeight();
	    width = bmpOriginal.getWidth();    
	    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	    Canvas c = new Canvas(bmpGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    paint.setColorFilter(f);
	    c.drawBitmap(bmpOriginal, 0, 0, paint);
	    return bmpGrayscale; 	
	}
	
	/**
	 * This method get the energy map of input image
	 * @param BufferedImage -- > img
	 * @return BufferedImage  --> output_img 
	 */
	public static Bitmap gradientFilter (Bitmap img){

		int width = img.getWidth();
		int height = img.getHeight();
		Bitmap temp_img1 = Bitmap.createBitmap(width, height, img.getConfig());
		Bitmap temp_img2 = Bitmap.createBitmap(width, height, img.getConfig());
		Bitmap output_img = Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8);
		
		double[][] matrix_vertical = { {-1.0F, 0.0F, 1.0F},
									{-1.0F, 0.0F, 1.0F},
									{-1.0F, 0.0F, 1.0F}};
		double[][] matrix_horizontal = { {1.0F,  1.0F,  1.0F},
									  	{0.0F,  0.0F,  0.0F},
									  	{-1.0F, -1.0F, -1.0F}};
		
		ConvolveOperation copv = new ConvolveOperation(3);
		copv.applyConfig(matrix_vertical);
		temp_img1 = ConvolveOperation.computeConvolution3x3(img, copv);
		
		ConvolveOperation coph = new ConvolveOperation(3);
		coph.applyConfig(matrix_horizontal);
		temp_img2 = ConvolveOperation.computeConvolution3x3(img, coph);
		
		for (int y = 0; y < height; ++y){
			for (int x = 0; x < width; ++x){
				float sum = 0.0f;
				sum = (   Math.abs(temp_img1.getPixel(x, y))
						+ Math.abs(temp_img2.getPixel(x, y)));
				output_img.setPixel(x, y, Math.round(sum));
			}
		}
		return output_img;		
	}
	
	/**
	 * This method remove the path from input image
	 * @param BufferedImage -- > img
	 * @return BufferedImage  --> removePathImg 
	 */
	public static Bitmap removePathFromImage(Bitmap img, int[] path){
		int width = img.getWidth();
		int height = img.getHeight();
		int band = 3;
		Bitmap removePathImg = Bitmap.createBitmap(width-1, height, img.getConfig());
		
		for (int y = 0; y < height; ++y){
			for (int x = 0; x <= path[y]-2; ++x){
				int temp = img.getPixel(x, y);
				removePathImg.setPixel(x, y, temp);
			}
			for (int x = path[y]-1; x < width-1; ++x){
				int temp = img.getPixel(x+1, y);
				removePathImg.setPixel(x, y, temp);
			}
		}
		return removePathImg;
	}
	/**
	 * This method remove the path from energy array
	 * @param double[][] -- > cumulativeEnergyArray
	 * @return double[][]  --> new_cumulativeEnergyArray 
	 */
	public static double[][] removePathEnergyArray(double[][] cumulativeEnergyArray, int[] path){
		int width = cumulativeEnergyArray[0].length;
		int height = cumulativeEnergyArray.length;
		double[][] new_cumulativeEnergyArray = new double[height][width-1];
		for (int y = 0; y < height; ++y){
			for (int x = 0; x <= path[y]-1; ++x){
				new_cumulativeEnergyArray[y][x] = cumulativeEnergyArray[y][x];
			}
			for (int x = path[y]; x < width-1; ++x){
				new_cumulativeEnergyArray[y][x] = cumulativeEnergyArray[y][x+1];
			}
		}
		return new_cumulativeEnergyArray;
	}
	
	/**
	 * This method get the index of min element in input array
	 * @param double[] -- > numbers
	 * @return int  --> minIndex 
	 */
	public static int getMinIndex(double[] numbers){  
		double minValue = numbers[0];
		int minIndex = 0;
		for(int i=0;i<numbers.length;i++){  
			if(numbers[i] < minValue){  
				minValue = numbers[i];
				minIndex = i;
			}  
		}
		return minIndex;  
	}  
	
	/**
	 * This method get the min value in input array
	 * @param double[] -- > numbers
	 * @return double  --> minValue 
	 */
	public static double getMinValue(double[] numbers){  
		double minValue = numbers[0];
		for(int i=0;i<numbers.length;i++){  
			if(numbers[i] < minValue){  
				minValue = numbers[i];
			}  
		}
		return minValue;  
	}
	
	/**
	 * This method calculate the cumulative energy array
	 * @param BufferedImage -- > img
	 * @return double[][]  --> cumulative_energy_array 
	 */
	public static double[][] getCumulativeEnergyArray (Bitmap img){
		int width = img.getWidth();
		int height = img.getHeight();
		double[][] cumulative_energy_array = new double[height][width];
		
		for (int y = 1; y < height; ++y){
			for (int x = 1; x < width-1; ++x){
				cumulative_energy_array[y][x] = (double)img.getPixel(x,y);
			}
		}
		
		for (int y = 1; y < height; ++y){
			for (int x = 1; x < width-1; ++x){
				double temp = 0.0;
				double tempArray3[] = new double[3];
				tempArray3[0] = cumulative_energy_array[y-1][x-1];
				tempArray3[1] = cumulative_energy_array[y-1][x];
				tempArray3[2] = cumulative_energy_array[y-1][x+1];
				temp = getMinValue(tempArray3) + (double)img.getPixel(x,y);
				cumulative_energy_array[y][x] = temp;
			}
		}
		return cumulative_energy_array;
	}
	
	/**
	 * This method find the minimum cost path from 
	 * cumulative energy array
	 * @param double[][] -- > cumulativeEnergyArray
	 * @return int[]  --> path 
	 */
	public static int[] findPath (double[][] cumulativeEnergyArray){
		int width = cumulativeEnergyArray[0].length;
		int height = cumulativeEnergyArray.length;
		int[] path = new int[height];
		
		double[] tempArray = new double[width-10];
		int y = height-1;
		for (int x = 5; x < width-5; ++x){
			tempArray[x-5] = cumulativeEnergyArray[y][x];
		}
		
		int ind_bot = getMinIndex(tempArray)+5;
		System.out.println("\nThe bottom index is: "+ind_bot);
		path[height-1] = ind_bot;
		
		int ind_temp = 0;
		double[] tempArray2 = new double[3];
		for (int i = height-1; i > 0; --i){
			tempArray2[0] = cumulativeEnergyArray[i-1][path[i]-1];
			tempArray2[1] = cumulativeEnergyArray[i-1][path[i]];
			tempArray2[2] = cumulativeEnergyArray[i-1][path[i]+1];
			ind_temp = getMinIndex(tempArray2);
			path[i-1] = path[i] + ind_temp - 1;
			if (path[i-1] <= 0){
				path[i-1] = 1;
			}
			else if (path[i-1] >= width-1){
				path[i-1] = width-2;
			}
		}
		return path;
	}
	
	/**
	 * This method enlarge the width of energy image 
	 * to prevent the boundary effect from convolution
	 * @param BufferedImage -- > eg. img
	 * @return enlarge (width) image --> enlarge_energy_img
	 */
	public static Bitmap enlargeEnergy (Bitmap img){
		int width = img.getWidth();
		int height = img.getHeight();
		Bitmap enlarge_energy_img = Bitmap.createBitmap(width+2, height, img.getConfig());
		for (int y = 0; y < height; ++y){
			for (int x = 1; x < width+1; ++x){
				enlarge_energy_img.setPixel(x, y, img.getPixel(x-1, y));
			}
		}
		for (int x = 0; x < 10; ++x){
			for (int y = 0; y < height; ++y){
				enlarge_energy_img.setPixel(x, y, 255);
			}
		}
		for (int x = width+1; x > width-9; --x){
			for (int y = 0; y < height; ++y){
				enlarge_energy_img.setPixel(x, y, 255);
			}
		}
		return enlarge_energy_img;
	}
}
