//By Ryan Chen
import java.io.*;
import java.util.*;

public class Runner 
{
	public static void main(String[] args) throws IOException
	{
		int [][] arr = new int[9][9];
		
		// name of the puzzle file
		String filename = "evil.txt";
		
		/* get the puzzle file in text format with numbers separated by spaces and 0 representing empty cells
		 * 
		 * Format example:
		 * 0 0 0 0 9 0 0 0 0
		 * 0 0 0 0 0 5 0 2 8
		 * 0 8 0 7 3 0 0 0 9
         * 0 1 0 0 0 0 8 0 6
         * 0 5 0 0 6 0 0 7 0
         * 4 0 2 0 0 0 0 5 0
         * 6 0 0 0 2 9 0 4 0
         * 9 4 0 5 0 0 0 0 0
         * 0 0 0 0 8 0 0 0 0
		 */
		Scanner scanner = new Scanner(new File(filename));
		
		for(int r = 0; r < 9; r++)
		{
			for(int c = 0; c < 9; c++)
			{
				arr[r][c] = scanner.nextInt();
			}
		}
		
		Board board = new Board(arr);
		board.run();
	}

}
