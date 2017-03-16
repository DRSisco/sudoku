package rosehulman.edu.solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Sudoku {
	private static int boardSize = 0;
	private static int partitionSize = 0;
	
	public static void main(String[] args){
		String filename = args[0];
		File inputFile = new File(filename);
		Scanner input = null;
		int[][] vals = null;
		int temp = 0;
    	int count = 0;
    	
	    try {
			input = new Scanner(inputFile);
			temp = input.nextInt();
			boardSize = temp;
			partitionSize = (int) Math.sqrt(boardSize);
			System.out.println("Boardsize: " + temp + "x" + temp);
			vals = new int[boardSize][boardSize];
			
			System.out.println("Input:");
	    	int i = 0;
	    	int j = 0;
	    	while (input.hasNext()){
	    		temp = input.nextInt();
	    		count++;
	    		System.out.print(temp);
				vals[i][j] = temp;
				j++;
				if (j == boardSize) {
					j = 0;
					i++;
					System.out.println();
				}
				if (j == boardSize) {
					break;
				}
	    	}
	    	input.close();
	    }
	    catch (FileNotFoundException exception) {
		    System.out.println("Input file not found: " + filename);
	    } 
	    catch (IOException e) {
	    	System.out.println(e);
		} 
	    if (count != boardSize*boardSize) throw new RuntimeException("Incorrect number of inputs.");

	    // Solve something
		
		// Output
		System.out.println();
		System.out.println("Output");
	    System.out.println();
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				System.out.printf("%3d", vals[i][j]);
			}
			System.out.println();
		}		
		
	}	
}