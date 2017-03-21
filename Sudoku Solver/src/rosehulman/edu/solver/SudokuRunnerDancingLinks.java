package rosehulman.edu.solver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import rosehulman.edu.solver.DancingLinks.DancingLinkNode;

public class SudokuRunnerDancingLinks {

	public static void main(String[] args) {
		Scanner input = null;
		String filename = args[0];
		int temp = 0;
		int count = 0;
		try {
			input = new Scanner(new File(filename));
			temp = input.nextInt();
			int boardSize = temp;
			System.out.println("Boardsize: " + temp + "x" + temp);
			int[][] start = new int[boardSize][boardSize];
			int i = 0;
			int j = 0;
			while (input.hasNext()) {
				temp = input.nextInt();
				count++;
				start[i][j] = temp;
				j++;
				if (j == boardSize) {
					j = 0;
					i++;
				}
				if (i == boardSize)
					break;
			}
			input.close();
			if (count != boardSize * boardSize) {
				throw new RuntimeException("Incorrect number of inputs.");
			}
			SudokuDLX sudoku = new SudokuDLX();
			sudoku.solve(start, filename);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}

class SudokuDLX {
	private int S = 9; // size of the board
	private int side = 3; // how long the side is

	public boolean solve(int[][] sudoku, String filename) {
		if (!validateSudoku(sudoku)) {
			System.out.println("Error: Invalid sudoku. Aborting....");
			return false;
		}
		S = sudoku.length;
		side = (int) Math.sqrt(S);
		runSolver(sudoku, filename);
		return true;
	}

	// sudoku has numbers 1-9. A 0 indicates an empty cell that we will need to
	// fill in.
	private int[][] makeExactCoverGrid(int[][] sudoku) {
		int[][] R = sudokuExactCover();
		for (int i = 0; i < S; i++) {
			for (int j = 0; j < S; j++) {
				int n = sudoku[i][j];
				if (n != 0) { // zero out in the constraint board
					for (int num = 1; num <= S; num++) {
						if (num != n) {
							Arrays.fill(R[getColumn(i, j, num)], 0);
						}
					}
				}
			}
		}
		return R;
	}

	private int getColumn(int row, int col, int num) {
		return (row) * S * S + (col) * S + (num - 1);
	}

	// Returns the base exact cover grid for a SUDOKU puzzle
	private int[][] sudokuExactCover() {
		int[][] R = new int[S * S * S][S * S * 4];
		int matrixDepth = 0;
		// row-column constraints
		for (int r = 0; r < S; r++) {
			for (int c = 0; c < S; c++, matrixDepth++) {
				for (int n = 1; n <= S; n++) {
					R[getColumn(r, c, n)][matrixDepth] = 1;
				}
			}
		}
		// Row Rule
		for (int r = 0; r < S; r++) {
			for (int n = 1; n <= S; n++, matrixDepth++) {
				for (int c1 = 0; c1 < S; c1++) {
					R[getColumn(r, c1, n)][matrixDepth] = 1;
				}
			}
		}
		// Column Rule
		for (int c = 0; c < S; c++) {
			for (int n = 1; n <= S; n++, matrixDepth++) {
				for (int r1 = 0; r1 < S; r1++) {
					R[getColumn(r1, c, n)][matrixDepth] = 1;
				}
			}
		}
		// Box Rule
		for (int br = 0; br < S; br += side) {
			for (int bc = 0; bc < S; bc += side) {
				for (int n = 1; n <= S; n++, matrixDepth++) {
					for (int rDelta = 0; rDelta < side; rDelta++) {
						for (int cDelta = 0; cDelta < side; cDelta++) {
							R[getColumn(br + rDelta, bc + cDelta, n)][matrixDepth] = 1;
						}
					}
				}
			}
		}
		return R;
	}

	public static boolean validateSudoku(int[][] grid) {
		for (int i = 0; i < grid.length; i++) {
			if (grid[i].length != grid.length)
				return false;
			for (int j = 0; j < grid[i].length; j++) {
				if (!(i >= 0 && i <= grid.length))
					return false; // 0 means not filled in
			}
		}

		int N = grid.length;

		boolean[] b = new boolean[N + 1];

		// Rows
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (grid[i][j] == 0)
					continue;
				if (b[grid[i][j]])
					return false;
				b[grid[i][j]] = true;
			}
			Arrays.fill(b, false);
		}

		// Columns
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (grid[j][i] == 0)
					continue;
				if (b[grid[j][i]])
					return false;
				b[grid[j][i]] = true;
			}
			Arrays.fill(b, false);
		}

		// Box
		int side = (int) Math.sqrt(N);
		for (int i = 0; i < N; i += side) {
			for (int j = 0; j < N; j += side) {
				for (int d1 = 0; d1 < side; d1++) {
					for (int d2 = 0; d2 < side; d2++) {
						if (grid[i + d1][j + d2] == 0)
							continue;
						if (b[grid[i + d1][j + d2]])
							return false;
						b[grid[i + d1][j + d2]] = true;
					}
				}
				Arrays.fill(b, false);
			}
		}
		return true;
	}

	private void runSolver(int[][] sudoku, String filename) {
		// Start the timer
		long startTime = System.nanoTime();

		int[][] cover = makeExactCoverGrid(sudoku);

		DancingLinks dancingLinkProblem = new DancingLinks(cover, this);

		// End Setup timer
		long endTime = System.nanoTime();

		long setupDuration = (endTime - startTime);
		
		startTime = endTime;

		dancingLinkProblem.runSolver(filename);

		// End timer
		endTime = System.nanoTime();

		long duration = (endTime - startTime);
		System.out.println("---------------------------------");
		System.out.println("Took: " + setupDuration + "ns to setup CoverGrid");
		System.out.println("Took: " + duration + "ns to solve");
		System.out.println("Took: " + (duration + setupDuration) + "ns in total");
		System.out.println("---------------------------------");
		System.out.println("Took: " + (double)setupDuration / 1000000000.0 + "s to setup CoverGrid");
		System.out.println("Took: " + (double)duration / 1000000000.0 + "s to solve");
		System.out.println("Took: " + (double)(duration + setupDuration) / 1000000000.0 + "s in total");
	}

	public void handleSolution(List<DancingLinkNode> answer, String filename) {
		int[][] result = parseBoard(answer);
		printSolution(result, filename);
	}

	public static void printSolution(int[][] result, String filename) {
		int N = result.length;
		String temp = filename;
		temp = temp.substring(0, temp.length() - 4);
		PrintWriter writer;
		try {
			writer = new PrintWriter(temp + "Soultion.txt", "UTF-8");
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < N; j++) {
					writer.printf("%3d", result[i][j]);
				}
				writer.println();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < N; i++) {
			String ret = "";
			for (int j = 0; j < N; j++) {
				ret += result[i][j] + " ";
			}
			System.out.println(ret);
		}
		System.out.println();
	}

	private int[][] parseBoard(List<DancingLinkNode> answer) {
		int[][] result = new int[S][S];
		for (DancingLinkNode n : answer) {
			DancingLinkNode rcNode = n;
			int min = Integer.parseInt(rcNode.C.name);
			for (DancingLinkNode tmp = n.right; tmp != n; tmp = tmp.right) {
				int val = Integer.parseInt(tmp.C.name);
				if (val < min) {
					min = val;
					rcNode = tmp;
				}
			}
			int pos = Integer.parseInt(rcNode.C.name);
			int val = Integer.parseInt(rcNode.right.C.name);
			int r = pos / S;
			int c = pos % S;
			int num = (val % S) + 1;
			result[r][c] = num;
		}
		return result;
	}
}

class DancingLinks {
	private ColumnNode header;
	private int updates = 0;
	private SudokuDLX handler;
	private List<DancingLinkNode> answer;

	public DancingLinks(int[][] grid, SudokuDLX h) {
		header = setupDancingLink(grid);
		handler = h;
	}

	public void runSolver(String filename) {
		updates = 0;
		answer = new LinkedList<DancingLinkNode>();
		if (search(0, filename)) {
			System.out.println("Number of updates: " + updates);
		} else {
			System.out.println("Failed after: " + updates + " updates");
			try {
				String temp = filename;
				temp = temp.substring(0, temp.length() - 4);
				PrintWriter writer = new PrintWriter(temp + "Soultion.txt", "UTF-8");
				writer.println("Puzzle has no solutions!");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean search(int k, String filename) {
		if (header.right == header) {
			System.out.println("\nSolution:\n");
			handler.handleSolution(answer, filename);
			return true;
		} else {
			ColumnNode c = selectColumnNodeHeuristic();
			c.cover();

			for (DancingLinkNode r = c.bottom; r != c; r = r.bottom) {
				answer.add(r);

				for (DancingLinkNode j = r.right; j != r; j = j.right) {
					j.C.cover();
				}

				if (search(k + 1, filename)) return true;

				r = answer.remove(answer.size() - 1);
				c = r.C;

				for (DancingLinkNode j = r.left; j != r; j = j.left) {
					j.C.uncover();
				}
			}
			c.uncover();
		}
		return false;
	}

	private ColumnNode selectColumnNodeHeuristic() {
		int min = Integer.MAX_VALUE;
		ColumnNode ret = null;
		for (ColumnNode c = (ColumnNode) header.right; c != header; c = (ColumnNode) c.right) {
			if (c.size < min) {
				min = c.size;
				ret = c;
			}
		}
		return ret;
	}

	/**
	 * Transforms the given grid into a 2D Linked List as described by Donald E.
	 * Knuth in his paper about Dancing Links
	 * 
	 * @param grid
	 *            CoverGrid of the Sudoku Puzzle
	 * @return ColumnNode the node of the far left Column node
	 */
	private ColumnNode setupDancingLink(int[][] grid) {
		int rowLength = grid.length;
		int columnLength = grid[0].length;

		ColumnNode headerNode = new ColumnNode("header");
		ArrayList<ColumnNode> columnNodes = new ArrayList<ColumnNode>();

		for (int i = 0; i < columnLength; i++) {
			ColumnNode n = new ColumnNode(Integer.toString(i));
			columnNodes.add(n);
			headerNode = (ColumnNode) headerNode.hookRight(n);
		}
		headerNode = (ColumnNode) headerNode.right; // Back to the starting one.
		for (int i = rowLength - 1; i >= 0; i--) {
			DancingLinkNode prev = null;
			for (int j = 0; j < columnLength; j++) {
				if (grid[i][j] == 1) {
					ColumnNode col = columnNodes.get(j);
					DancingLinkNode newNode = new DancingLinkNode(col);
					col.hookDown(newNode);
					if (prev == null)
						prev = newNode;
					prev = prev.hookRight(newNode);
					col.size++;
				}
			}
		}
		headerNode.size = columnLength;
		return headerNode;
	}

	class DancingLinkNode {
		DancingLinkNode left, right, top, bottom;
		ColumnNode C;

		public DancingLinkNode() {
			this.left = this;
			this.right = this;
			this.top = this;
			this.bottom = this;
		}

		public DancingLinkNode(ColumnNode c) {
			this.left = this;
			this.right = this;
			this.top = this;
			this.bottom = this;
			this.C = c;
		}

		// Used to setup 2D linked List
		DancingLinkNode hookDown(DancingLinkNode nodeToReAdd) {
			// assert (this.C == n1.C);
			nodeToReAdd.bottom = this.bottom;
			nodeToReAdd.bottom.top = nodeToReAdd;
			nodeToReAdd.top = this;
			this.bottom = nodeToReAdd;
			return nodeToReAdd;
		}

		// Used to setup 2D linked List
		DancingLinkNode hookRight(DancingLinkNode nodeToReAdd) {
			nodeToReAdd.right = this.right;
			nodeToReAdd.right.left = nodeToReAdd;
			nodeToReAdd.left = this;
			this.right = nodeToReAdd;
			return nodeToReAdd;
		}

		// (1) in Paper
		void unlinkLR() {
			this.left.right = this.right;
			this.right.left = this.left;
			updates++;
		}
		
		// (1) in Paper
		void unlinkUD() {
			this.top.bottom = this.bottom;
			this.bottom.top = this.top;
			updates++;
		}

		// (2) in Paper
		void relinkLR() {
			this.left.right = this.right.left = this;
			updates++;
		}

		// (2) in Paper
		void relinkUD() {
			this.top.bottom = this.bottom.top = this;
			updates++;
		}
	}

	class ColumnNode extends DancingLinkNode {
		int size; // count of ones in current column
		String name;

		public ColumnNode(String n) {
			super();
			size = 0;
			name = n;
			C = this;
		}

		void cover() {
			unlinkLR();
			for (DancingLinkNode i = this.bottom; i != this; i = i.bottom) {
				for (DancingLinkNode j = i.right; j != i; j = j.right) {
					j.unlinkUD();
					j.C.size--;
				}
			}
			header.size--;
		}

		void uncover() {
			for (DancingLinkNode i = this.top; i != this; i = i.top) {
				for (DancingLinkNode j = i.left; j != i; j = j.left) {
					j.C.size++;
					j.relinkUD();
				}
			}
			relinkLR();
			header.size++;
		}
	}
}