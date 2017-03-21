package rosehulman.edu.solver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class SudokuRunner {
	public static void main(String[] args){
		Sudoku sudoku = new Sudoku(args[0]);
		sudoku.solve();
		sudoku.printGrid();
	}	
}

class Position {
	private int column;
	private int row;

	public Position(int row, int column) {
		super();
		this.column = column;
		this.row = row;
	}
	
	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}
	
	@Override
	public String toString() {
		return "Row:" + this.getRow() + "  Column:" + this.getColumn(); 
	}
}

class Sudoku {
	private int boardSize = 0;
	private int partitionSize = 0;
	private int[][] vals;
	private int[][] start;
	private boolean isValid = true;
	private String filename;

	public Sudoku(String filename) {
		super();
		this.filename = filename;
		Scanner input = null;
		int temp = 0;
		int count = 0;
		try {
			File inputFile = new File(filename);
			input = new Scanner(inputFile);
			temp = input.nextInt();
			boardSize = temp;
			partitionSize = (int) Math.sqrt(boardSize);
			System.out.println("Boardsize: " + temp + "x" + temp);
			start = new int[boardSize][boardSize];
			int i = 0;
			int j = 0;
			while (input.hasNext()) {
				temp = input.nextInt();
				count++;
				this.start[i][j] = temp;
				j++;
				if (j == boardSize) {
					j = 0;
					i++;
				}
				if (i == boardSize) {
					break;
				}
			}
			input.close();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			this.isValid = false;
		}
		if (count != boardSize * boardSize) {
			this.isValid = false;
			throw new RuntimeException("Incorrect number of inputs.");
		}
	}

	public void solve() {
		// Start vals to be the same as the starting puzzle.
		this.vals = new int[boardSize][boardSize];
		for (int i = 0; i < this.boardSize; i++) {
			for (int j = 0; j < this.boardSize; j++) {
				this.vals[i][j] = this.start[i][j];
			}
		}
		System.out.println("Solving...");
		// Start the timer
		long startTime = System.nanoTime();

		// Solve puzzle
		solveEasy();
		if (solveRecursive(nextSpotToSolve(new Position(0, 0)))) {
			System.out.println("Success!");
		} else {
			System.out.println("Impossible Puzzle");
		}

		// End timer
		long endTime = System.nanoTime();

		long duration = (endTime - startTime);
		System.out.println("Took: " + duration + "ns to solve");
	}
	
	private ArrayList<Position> solveEasy(){
		ArrayList<Position> result = new ArrayList<Position>();
		boolean flag = false;
		for(int i = 0; i < this.boardSize; i++) {
			for (int j = 0; j < this.boardSize; j++) {
				if (this.vals[i][j] == 0) {
					ArrayList<Integer> possible = getValidValues(new Position(i,j));
					if (possible.size() == 1) {
						this.vals[i][j] = possible.get(0);
						result.add(new Position(i,j));
						flag = true;
					} else if (true) {
						
					}
				}
			}
		}
		if (flag) {
			result.addAll(solveEasy());
		}
		return result;
	}

	private boolean solveRecursive(Position p) {
		if (p == null) {
			return true; // No more spaces to solve!
		}
//		ArrayList<Integer> validValues = getValidValues(p);
//		for (int i : validValues) {
		for (int i = 1; i <= this.boardSize; i++) {
			if (validValue(p, i)) { // Forward checking by making sure this is a
									// valid move
				this.vals[p.getRow()][p.getColumn()] = i;
				ArrayList<Position> easyMoves = solveEasy();
				if (solveRecursive(nextSpotToSolve(p))) {
					return true;
				}
				for (Position pos: easyMoves) {
					this.vals[pos.getRow()][pos.getColumn()] = 0;
				}
			}
		}
		this.vals[p.getRow()][p.getColumn()] = 0;
		return false;
	}

	private ArrayList<Integer> getValidValues(Position p) {
		ArrayList<Integer> result = new ArrayList<Integer>(this.boardSize);
		for (int i = 1; i <= this.boardSize; i++) {
			result.add(i);
		}
		removeRows(p, result);
		removeColumns(p,result);
		removeBox(p,result);
		return result;
	}

	private Position nextSpotToSolve(Position p) {
		int jStart = p.getColumn();
		for (int i = p.getRow(); i < this.boardSize; i++) {
			for (int j = jStart; j < this.boardSize; j++) {
				if (this.vals[i][j] == 0) {
					return new Position(i, j);
				}
			}
			jStart = 0;
		}
		return null;
	}

	private boolean validValue(Position p, int val) {
		return checkRow(p, val) && checkColumn(p, val) && checkBox(p, val);
	}

	private void removeRows(Position p, ArrayList<Integer> result) {
		for (int j = 0; j < this.boardSize; j++) {
			if (result.contains(this.vals[p.getRow()][j])) {
				result.remove((Object) this.vals[p.getRow()][j]);
			}
		}
	}

	private void removeColumns(Position p, ArrayList<Integer> result) {
		for (int j = 0; j < this.boardSize; j++) {
			if (result.contains(this.vals[j][p.getColumn()])) {
				result.remove((Object) this.vals[j][p.getColumn()]);
			}
		}
	}

	private boolean removeBox(Position p, ArrayList<Integer> result) {
		Position boxStart = getBoxStart(p);
		for (int i = boxStart.getRow(); i < boxStart.getRow() + this.partitionSize; i++) {
			for (int j = boxStart.getColumn(); j < boxStart.getColumn() + this.partitionSize; j++) {
				if (result.contains(this.vals[i][j])) {
					result.remove((Object) this.vals[i][j]);
				}
			}
		}
		return true;
	}
	
	private boolean checkRow(Position p, int val) {
		for (int j = 0; j < this.boardSize; j++) {
			if (j != p.getColumn()) {
				if (this.vals[p.getRow()][j] == val) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkColumn(Position p, int val) {
		for (int i = 0; i < this.boardSize; i++) {
			if (i != p.getRow()) {
				if (this.vals[i][p.getColumn()] == val) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkBox(Position p, int val) {
		Position boxStart = getBoxStart(p);
		for (int i = boxStart.getRow(); i < boxStart.getRow() + this.partitionSize; i++) {
			for (int j = boxStart.getColumn(); j < boxStart.getColumn() + this.partitionSize; j++) {
				if (i != p.getRow() || j != p.getColumn()) {
					if (this.vals[i][j] == val) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private Position getBoxStart(Position p) {
		return new Position((p.getRow() / this.partitionSize) * this.partitionSize,
				(p.getColumn() / this.partitionSize) * this.partitionSize);
	}

	public void printGrid() {
		if (this.isValid) {
			// Input
			System.out.println();
			System.out.println("Input");
			System.out.println();
			for (int i = 0; i < boardSize; i++) {
				for (int j = 0; j < boardSize; j++) {
					System.out.printf("%3d", start[i][j]);
				}
				System.out.println();
			}
			String temp = this.filename;
			temp = temp.substring(0, temp.length() - 4);
		    PrintWriter writer;
			try {
				writer = new PrintWriter(temp + "Soultion.txt", "UTF-8");
				for (int i = 0; i < boardSize; i++) {
					for (int j = 0; j < boardSize; j++) {
						writer.printf("%3d", this.vals[i][j]);
					}
					writer.println();
				}
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Output
			System.out.println();
			System.out.println("Output");
			System.out.println();
			for (int i = 0; i < boardSize; i++) {
				for (int j = 0; j < boardSize; j++) {
					System.out.printf("%3d", this.vals[i][j]);
				}
				System.out.println();
			}
		} else {
			System.out.println("This is not a valid Puzzle, try again.");
		}
	}
}