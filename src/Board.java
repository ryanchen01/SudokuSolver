//By Ryan Chen
import java.util.*;
import static java.lang.System.*;

public class Board 
{
	private int[][] board;					//matrix of the game board
	private ArrayList<Integer>[][] state;	//matrix of ArrayLists containing possible answers
	private int zeroCnt;					//number of zeros in board
	private int stateCnt;					//number of populated states in state
	
	public Board(int[][] board)
	{
		this.board = board;
		state = new ArrayList[9][9];
		zeroCnt = 0;
		initialize();
	}
	
	// initialize state with 1-9 as possible answer
	private void initialize()
	{
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				state[r][c] = new ArrayList<Integer>();
				if(board[r][c] == 0)
				{
					stateCnt++;
					zeroCnt++;
					for(int i = 1; i <= 9; i++)
					{
						state[r][c].add(i);
					}
				}
			}
		}
	}
	
	//run the solver
	public void run()
	{
		int zeroCnt = this.zeroCnt;
		out.println("start");

		solve(board,state,stateCnt,zeroCnt);
	}
	
	//solve the board recursively
	private boolean solve(int[][] board, ArrayList<Integer>[][] state, int stateCnt, int zeroCnt)
	{
		int prevZeroCnt;
		zeroCnt = countZero(board);
		out.println("Before:");
		printStatus(board,state);
		//fill in known answers with current board
		do {
			prevZeroCnt = zeroCnt;
			stateCnt = 0; zeroCnt = 0;
			// reinitialize state to calibrate
			for(int r = 0; r < 9; r++)
			{
				for(int c = 0; c < 9; c++)
				{
					state[r][c] = new ArrayList<Integer>();
					if(board[r][c] == 0)
					{
						stateCnt++;
						zeroCnt++;
						for(int i = 1; i <= 9; i++)
						{
							state[r][c].add(i);
						}
					}
				}
			}
			reduce(board,state);
			zeroCnt = onlyChoice(board,state,stateCnt,zeroCnt);
			nakedTwin(board,state,stateCnt,zeroCnt);
			printStatus(board,state);
		}while(prevZeroCnt > zeroCnt && !isSolved(board));
		
		if(isSolved(board))
		{
			return true;
		}
		
		// end current instance if solving is not possible
		if(countStates(state) == 0 || countZero(board) <= 2)
		{
			return false;
		}
		
		
		int[][] newBoard = copyBoard(board);														// create a copy of board
		ArrayList<Integer>[][] newState = copyState(state);											// create a copy of state
		int[] lowestCell = getLowest(state);														// get location of a state with least possible answers
		
		// try solving with each possible answer
		for(int i = 0; i < state[lowestCell[0]][lowestCell[1]].size(); i++)
		{
			newBoard[lowestCell[0]][lowestCell[1]] = state[lowestCell[0]][lowestCell[1]].get(i);
			newState[lowestCell[0]][lowestCell[1]] = new ArrayList<Integer>();
			boolean done = solve(newBoard,newState,countStates(newState), countZero(newBoard));
			if(done)
				return true;
			newBoard = copyBoard(board);
			newState = copyState(state);
		}
		return false;
	}
	
	// reduce possible answers in state with constraint propagation and fill in the answer if it is the only possible answer
	public int onlyChoice(int[][] board, ArrayList<Integer>[][] state, int stateCnt, int zeroCnt)
	{
		while(!isSolved(board))
		{
			reduce(board,state);	// reduce possible answers in state with constraint propagation
			int prevZeroCnt = zeroCnt;
			
			// fill in the answer if it is the only possible answer
			for(int r = 0; r < 9; r++)
			{
				for(int c = 0; c < 9; c++)
				{
					if(board[r][c] == 0 && state[r][c].size() == 1)
					{
						board[r][c] = state[r][c].get(0);
						state[r][c] = new ArrayList<Integer>();
						zeroCnt--;
						stateCnt--;
					}
				}
			}
			
			// end loop if there are no more possible reduces
			if(prevZeroCnt == zeroCnt)
			{
				return zeroCnt;
			}
		}
		return zeroCnt;
	}
	
	// reduce possible answers in state with the naked-twin technique
	private void nakedTwin(int[][] board, ArrayList<Integer>[][] state, int stateCnt, int zeroCnt)
	{
		for(int r = 0 ; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				if(board[r][c] == 0)
				{
					for(int i = 0; i < 9; i++)
					{
						//if has two same state with two elements in the same column
						if(r != i && board[i][c] == 0 && state[r][c].size() == 2 && state[r][c].equals(state[i][c]))
						{
							nakedTwinCol(r,c,i,board,state);					// remove same possible answers from states other than the two that are equal in their column
						}
						
						//if has two same state with two elements in the same row
						if(c != i && board[r][i] == 0 && state[r][c].size() == 2 && state[r][c].equals(state[r][i]))
						{
							nakedTwinRow(r,c,i,board,state);					// remove same possible answers from states other than the two that are equal in their row
						}
					}
					
				}
			}
		}
	}
	
	// remove same possible answers from states other than the two that are equal in a column
	private void nakedTwinCol(int row, int col, int row2,int[][] board, ArrayList<Integer>[][] state)
	{
		for(int i = 0; i < 9; i++)
		{
			if(i != row && i != row2 && board[i][col] == 0)
			{
				if(state[i][col].indexOf(state[row][col].get(0)) != -1)
					state[i][col].remove(state[i][col].indexOf(state[row][col].get(0)));
				if(state[i][col].indexOf(state[row][col].get(1)) != -1)
					state[i][col].remove(state[i][col].indexOf(state[row][col].get(1)));
			}
		}
	}
	
	// remove same possible answers from states other than the two that are equal in a row
	private void nakedTwinRow(int row, int col, int col2,int[][] board, ArrayList<Integer>[][] state)
	{
		for(int i = 0; i < 9; i++)
		{
			if(i != col && i != col2 && board[row][i] == 0)
			{
				if(state[row][i].indexOf(state[row][col].get(0)) != -1)
					state[row][i].remove(state[row][i].indexOf(state[row][col].get(0)));
				if(state[row][i].indexOf(state[row][col].get(1)) != -1)
					state[row][i].remove(state[row][i].indexOf(state[row][col].get(1)));
			}
		}
	}
	
	// get location of a state with fewest possible answers
	private int[] getLowest(ArrayList<Integer>[][] state)
	{
		int[] lowestCell = new int[2];
		int lowestRow = 0;
		int lowestCol = 0;
		int lowest = 10;
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				if(state[r][c].size() > 1 && state[r][c].size() < lowest)
				{
					lowestRow = r;
					lowestCol = c;
					lowest = state[r][c].size();
				}
			}
		}
		lowestCell[0] = lowestRow;
		lowestCell[1] = lowestCol;
		return lowestCell;
	}
	
	// count the zeros (blank spaces) in the board
	private int countZero(int[][] board)
	{
		int zeroCnt = 0;
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				if(board[r][c] == 0)
					zeroCnt++;
			}
		}
		return zeroCnt;
	}
	
	// count the number of populated states
	private int countStates(ArrayList<Integer>[][] state)
	{
		int stateCnt = 0;
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				if(state[r][c].size() > 0)
					stateCnt++;
			}
		}
		return stateCnt;
	}
	
	// reduce possible answers in state with constraint propagation
	private void reduce(int[][] board, ArrayList<Integer>[][] state)
	{
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				if(board[r][c] == 0)
				{
					reduceRow(r,c,board,state);
					reduceCol(r,c,board,state);
					reduceBlock(r,c,board,state);
				}
			}
		}
	}
	
	// reduce possible answers in a row with constraint propagation
	private void reduceRow(int r, int c,int[][] board, ArrayList<Integer>[][] state)
	{
		for(int i = 0; i < 9; i++)
		{
			if(i != c && state[r][c].indexOf(board[r][i]) != -1)
			{
				state[r][c].remove(state[r][c].indexOf(board[r][i]));
			}
		}
	}
	
	// reduce possible answers in a column with constraint propagation
	private void reduceCol(int r, int c,int[][] board, ArrayList<Integer>[][] state)
	{
		for(int i = 0; i < 9; i++)
		{
			if(i != r && state[r][c].indexOf(board[i][c]) != -1)
			{
				state[r][c].remove(state[r][c].indexOf(board[i][c]));
			}
		}
	}
	
	// reduce possible answers in their proper 3x3 block with constraint propagation
	private void reduceBlock(int row, int col,int[][] board, ArrayList<Integer>[][] state)
	{
		int[] startIndex = getStartIndex(row,col);
		for(int r = startIndex[0] ; r < startIndex[0] + 3; r++)
		{
			for(int c = startIndex[1]; c < startIndex[1] + 3; c++)
			{
				if(r != row && c != col && state[row][col].indexOf(board[r][c]) != -1)
				{
					state[row][col].remove(state[row][col].indexOf(board[r][c]));
				}
			}
		}
	}
	
	// get the starting index of the block that the cell is in
	private int[] getStartIndex(int r, int c)
	{
		int[] index = new int[2];
		
		if(r < 3)
			index[0] = 0;
		else if(r < 6)
			index[0] = 3;
		else
			index[0] = 6;
		
		if(c < 3)
			index[1] = 0;
		else if(c < 6)
			index[1] = 3;
		else
			index[1] = 6;
		
		return index;
	}
	
	// make a copy of the board and return it
	private int[][] copyBoard(int[][] board)
	{
		int[][] b = new int[9][9];
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				b[r][c] = board[r][c];
			}
		}
		return b;
	}
	
	// make a copy of the state and return it
	private ArrayList<Integer>[][] copyState(ArrayList<Integer>[][] state)
	{
		ArrayList<Integer>[][] s = new ArrayList[9][9];
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				s[r][c] = state[r][c];
			}
		}
		return s;
	}
	
	// check if the board is solved
	public boolean isSolved(int[][] board)
	{
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				if(board[r][c] == 0)
					return false;
				for(int i = 0; i < 9; i++)
				{
					if(i != r && board[r][c] == board[i][c])
						return false;
				}
				for(int i = 0; i < 9; i++)
				{
					if(i != c && board[r][c] == board[r][i])
						return false;
				}
				int[] startIndex = getStartIndex(r,c);
				for(int i = startIndex[0] ; i < startIndex[0] + 3; i++)
				{
					for(int j = startIndex[1]; j < startIndex[1] + 3; j++)
					{
						if(r != i && c != j && board[r][c] == board[i][j])
						{
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	// print the current board and all possible answers for each empty cell
	private void printStatus(int[][] board, ArrayList<Integer>[][] state)
	{
		String status = printBoard(board);
		
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				if(state[r][c].size() > 0)
				{
					status += "[" + r + "][" + c + "]: ";
					for(int i = 0; i < state[r][c].size(); i++)
					{
						status += state[r][c].get(i) + " ";
					}
					status += "\n";
				}
			}
		}
		
		out.println(status);
	}
	
	// format the board in to a String ready for printing
	public String printBoard(int[][] board)
	{
		String out = "Zero Count = " + countZero(board) + "\n    0   1   2   3   4   5   6   7   8\n  =========================================\n";
		for(int r = 0; r < 9; r++)
		{
			out += r+" ||";
			for(int c = 0; c < 9; c++)
			{
				if(board[r][c] == 0)
				{
					out += "   |";
					if(c == 2 || c == 5 || c == 8)
						out += "|";
				}
				else
				{
					out += " " + board[r][c] + " |";
					if(c == 2 || c == 5 || c == 8)
						out += "|";
				}
			}
			if(r == 2 || r == 5 || r == 8)
				out += "\n  =========================================\n";
			else
				out += "\n  -----------------------------------------\n";
			
		}
		return out;
	}
}
