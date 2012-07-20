import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;


public class DataGenerator {
	
	/* this method is used for loading a set of sets, each set consisting of some integers written in the same line
	 * the number of elements in each line (set) can be different.
	 */
	protected static ArrayList<int[]> loadIntSets(String filename) throws Exception {
		ArrayList<int[]> matrix = new ArrayList<int[]>();
		BufferedReader bis= new BufferedReader(new FileReader(filename));
		String input;
		while ((input=bis.readLine())!=null){
			String[] s=input.split("\\s");
			int[] row= new int[s.length];
			for(int i=0;i<s.length;i++){
				row[i]=Integer.parseInt(s[i]);
			}
			Arrays.sort(row);
			matrix.add(row);
		}

		return matrix;
	}
	
	/* this method is used for loading a set of sets, each set consisting of some integers written in the same line
	 * the number of elements in each line (set) can be different.
	 */
	protected static ArrayList<int[]> loadStringSets(Map<String, Integer> nameMapToInt, String filename) throws Exception {
		ArrayList<int[]> matrix = new ArrayList<int[]>();
		BufferedReader bis= new BufferedReader(new FileReader(filename));
		String input;
		while ((input=bis.readLine())!=null){
			String[] s=input.split("\\s");
			int[] row= new int[s.length];
			for(int i=0;i<s.length;i++){
				row[i]=nameMapToInt.get(s[i]);
			}
			Arrays.sort(row);
			matrix.add(row);
		}

		return matrix;
	}
	
	/*
	 * this method loads an 2-D array of double numbers from a text file. The matrix's dimensions are not known a priori.
	 */
	protected static double[][] load(String filename) throws Exception {
		double [][] data = null;
		int nCols = 0, nRows = 0;
		ArrayList<double[]> matrix = new ArrayList<double[]>();
		BufferedReader bis= new BufferedReader(new FileReader(filename));
		String input;
		while ((input=bis.readLine())!=null){
			String[] s=input.split("\\s");
			if (nCols==0) {
				nCols = s.length;
			} else if (nCols != s.length) {
				throw new Exception("Different number of columns: " + nCols + " != "+s.length);
			}				
			double[] rows= new double[s.length];
			for(int i=0;i<s.length;i++){
				rows[i]=Double.parseDouble(s[i]);
			}
			matrix.add(rows);
		}
		nRows = matrix.size();
		data = new double[nRows][nCols];
		for (int i=0; i<matrix.size(); ++i)
			for (int j=0; j<matrix.get(i).length; ++j)
				data[i][j] = matrix.get(i)[j];		
		
		return data;
	}
	
	public static double max(double data[]) {
		double max = -Double.MAX_VALUE;
		for (int i=0; i<data.length; ++i)
			if (data[i]>max) {
				max = data[i];
			}
		return max;
	}
	public static int max(int data[]) {
		int max = -Integer.MAX_VALUE;
		for (int i=0; i<data.length; ++i)
			if (data[i]>max) {
				max = data[i];
			}
		return max;
	}


	protected static String summarize(int[] set) {
		StringBuffer buf = new StringBuffer();
		for (int i=0; i<set.length; ++i) 
			buf.append(set[i]+"-");
		return buf.toString();
	}

	static ArrayList<String> nchoosek(int[] set, int k) {
		ArrayList<String> allSubsetSummaries = new ArrayList<String>();
	    int[] subset = new int[k];
	    processLargerSubsets(allSubsetSummaries, set, subset, 0, 0);
		return allSubsetSummaries;
	}

	static void processLargerSubsets(ArrayList<String> allSubsetSummaries, int[] set, int[] subset, int subsetSize, int nextIndex) {
	    if (subsetSize == subset.length) {
	        //process(subset);
	    	allSubsetSummaries.add(summarize(subset));
	    } else {
	        for (int j = nextIndex; j < set.length; j++) {
	            subset[subsetSize] = set[j];
	            processLargerSubsets(allSubsetSummaries, set, subset, subsetSize + 1, j + 1);
	        }
	    }
	}
	
	
	/**
	 * This method loads the solution of the GLPK package from a file and displays it in a human-readable fashion
	 * @param nameMapFile the filename of the mapping between the column names and column numbers
	 * @param solutionFile the filename of the output (solution) of the GLPK package
	 * @param inputParameterPrefix the prefix used in the name convention of the input files that contain the model parameters
	 * @throws Exception
	 */
	public static void AMPLshow(String nameMapFile, String solutionFile, String inputParameterPrefix) throws Exception {
		ColumnMap columnMap = new ColumnMap(nameMapFile);
		Map<Integer, String> nameMap = columnMap.getNumber2NameMapping();
		
//		double [][] data = null;
//		int nCols = 0, nRows = 0;
		ArrayList<Integer> build = new ArrayList<Integer>();
		BufferedReader bis= new BufferedReader(new FileReader(solutionFile));
		String input;		
		while ((input=bis.readLine())!=null){
			if (!input.toLowerCase().contains("build["))
				continue;			
			//otherwise let's parse it!
			StringTokenizer stok = new StringTokenizer(input, " ");
			String[] tokens = new String[stok.countTokens()];
			int count = 0;
			while(stok.hasMoreTokens()) {
				tokens[count++] = stok.nextToken();
			}
			if (tokens.length!=6)
				throw new Exception("Bad solution file format:" + tokens);
			
			build.add(Integer.parseInt(tokens[3]));
		}
		System.out.println("We read "+build.size()+" build varibles");
		
		ArrayList<int[]> candidates = loadStringSets(columnMap.getName2NumberMapping(), inputParameterPrefix + "candidates.txt");
		double[][] storage = load(inputParameterPrefix + "storage.txt");
		double[][] total_storage = load(inputParameterPrefix + "total_storage.txt");
		double[][] T = load(inputParameterPrefix + "T.txt");
		
		//calculate the total storage cost
		double optimized_cost = 0.0;
		int number_of_samples = 0;
		for (int i=0; i<storage.length; ++i) {
			if (build.get(i)==1) {
				++number_of_samples;
				optimized_cost += storage[i][0];
			} else if (build.get(i)!=0) {
				throw new Exception("Our solution is not integer!: "+i);
			}
 		}
		System.out.println("With T="+T[0][0]+" we chose " + number_of_samples + " using "+optimized_cost/total_storage[0][0] + 
				" of our storage allocation (used "+ optimized_cost + ").");
		for (int i=0; i<build.size(); ++i) {
			if (build.get(i)==1) {
				for (int j=0; j<candidates.get(i).length; ++j) 
					System.out.print(nameMap.get(candidates.get(i)[j])+ " ");
				System.out.println();
			} else if (build.get(i)!=0) {
				throw new Exception("Our solution is not integer!: "+i);
			}
 		}
		
	}

	
}
