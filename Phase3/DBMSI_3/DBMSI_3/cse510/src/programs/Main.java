package programs;

import java.io.BufferedReader;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeSet;

import joins.ColumnarNestedLoopsJoins;
import bitmap.BM;
import bitmap.BitMapFile;
import bitmap.BitmapEquiJoin;
import btree.BT;
import btree.BTFileScan;
import btree.BTreeFile;
import btree.IntegerKey;
import btree.KeyDataEntry;
import btree.StringKey;
import bufmgr.BufMgrException;
import bufmgr.HashOperationException;
import bufmgr.PageNotFoundException;
import bufmgr.PagePinnedException;
import bufmgr.PageUnpinnedException;
import columnar.ColumnarFile;
import columnar.ColumnarFileMetadata;
import columnar.TupleScan;
import diskmgr.PCounter;
import global.AttrOperator;
import global.AttrType;
import global.Convert;
import global.RID;
import global.SystemDefs;
import global.TID;
import global.TupleOrder;
import heap.HFBufMgrException;
import heap.HFDiskMgrException;
import heap.HFException;
import heap.HFPage;
import heap.Heapfile;
import heap.InvalidSlotNumberException;
import heap.Scan;
import heap.Tuple;
import index.ColumnarIndexScan;
import iterator.ColumnarSort;
import iterator.ColumnarFileScan;
import iterator.CondExpr;
import iterator.FileScan;
import iterator.FldSpec;
import iterator.RelSpec;
import joins.ColumnarNestedLoopsJoins;
//import programs.CDBTest.GetStuff;
import value.IntegerValue;
import value.StringValue;
import diskmgr.PCounter;

public class Main {

	public static String filePath = "/Users/sucharitharumesh/eclipse-workspace/DBMSI_3/cse510/Sampledata/";
	
	public static ColumnarFile outerBE =null; 
	public static ColumnarFile innerBE=null;

	public static Scanner scanner = new Scanner(System.in);

	public static List<String> bitmapIndex = new ArrayList<String>(); 

	private static int startRead = 0, startWrite = 0;

	public static Map<String, HashSet> bitmapMeta = new HashMap<>();
	public static Map<List<Integer>,List<Integer>> Join1 = null;
	public static Map<List<Integer>,List<Integer>> Join2 = null;
	public static Map<List<Integer>,List<Integer>> Join3 = null;


	public static String[] input1 = null;


	public static void main(String[] args) throws Exception {

		String dataFileName = args[0];
		String columnDbName = args[1];
		String columnName;
		String columnarFileName ="";
		int numberOfColumns= Integer.parseInt(args[3]);

		String queryInput = null;
		BufferedReader in;
		String[] colArray = null;
		String[] colArrayBE = null;
		//ColumnarFile outerBE =null; 
		//ColumnarFile innerBE=null;
		String[] input = null;
		String DBName ="";
		String filename ="";
		int numBufBE = 0;

		String targetColumnsBE ="";
		List<String> targetColumnNamesBE = new ArrayList<String>();
		


		System.out.println("************RUNNING TEST....!!!************");
		
		/*System.out.println("**********COLUMNAR SORT**********" );
		System.out.println("\nEnter the query in this format (sort COLUMNDBNAME COLUMNARFILENAME SORTCOLUMN SORTORDER NUMBUF)");
		SystemDefs sysDef = new SystemDefs(columnDbName,100000,100,"Clock");
		in = new BufferedReader(new InputStreamReader(System.in));
		try {
			queryInput = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		input = queryInput.split("\\s+");
		DBName = input[1];
		String fileName = input[2];
		int sortColumn = getColumnNumber(input[3]);

		ColumnarFile cf = insertRecords("smalldata_part1.txt"); 
		TupleOrder[] order = new TupleOrder[2];
		order[0] = new TupleOrder(TupleOrder.Ascending);
		order[1] = new TupleOrder(TupleOrder.Descending);

		TupleOrder sortOrder = null;
		if (input[4].equalsIgnoreCase("ASC")) {
			sortOrder = order[0];
		} else {
			sortOrder = order[1];
		}

		int numBuff = Integer.parseInt(input[5]);

		//createDatabaseIfNotExists(columnDBName, numBuff);

		// cf;
		performColumnarSort(cf,fileName, null, sortColumn, sortOrder, numBuff);
		System.out.println("**********COLUMNAR SORT SUCCESSFULLY COMPLETED**********" );
		*/
		//BITMAP EQUI JOIN
				try {
					
					Join1 = null;
					Join2 = null;
					Join3 = null;
					targetColumnsBE ="";
					List<CondExpr> LeftConst = new ArrayList<CondExpr>();
					List<CondExpr> RightConst = new ArrayList<CondExpr>();
					List<CondExpr> EquiConst = new ArrayList<CondExpr>();
					targetColumnNamesBE = new ArrayList<String>();
					
					
					System.out.println("**********BITMAP EQUALITY JOIN**********" );

					System.out.println("\nEnter the query in this format (bmj COLUMNDB OUTTERFILE INNERFILE [TARGETCOLUMNNAMES] [LEFTCONST] [RIGHTCONST] [EQUICONST] NUMBUF )");
					SystemDefs sysDef = new SystemDefs(columnDbName,100000,100,"Clock");	

					in = new BufferedReader(new InputStreamReader(System.in));
					try {
						queryInput = in.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
					input = queryInput.split("\\s+");
					String ColumnDB = input[1];
					String outerFile = input[2];
					String innerFile = input[3];
					targetColumnsBE = input[4];
					
					int startRead = 0;
					int startWrite = 0;
					//SystemDefs.JavabaseBM.resetAllPinCount();
					//SystemDefs.JavabaseBM.flushAllPages();
					startRead = PCounter.rcounter;
					startWrite = PCounter.wcounter;

					//SystemDefs sysDef = new SystemDefs(ColumnDB,100000,100,"Clock");		

					outerBE = insertRecords(outerFile+".txt"); 
					innerBE = insertRecords(innerFile+".txt");
					String yesno = "";
					do {

						System.out.println("Enter Columnar filename:");
						columnarFileName = scanner.next();
						System.out.println("Enter column name for index: ");
						columnName = scanner.next();
						//String name = columnarFileName+columnName;
						//bitmapIndex.add(name);
						if(columnarFileName.equals(outerFile))
							indexQueryBitmap(outerBE, ColumnDB, columnarFileName, columnName);
						else if(columnarFileName.equals(innerFile))
							indexQueryBitmap(innerBE, ColumnDB, columnarFileName, columnName);
						System.out.println("Do you want to create a BITMAP index again?(Y/N):");
						yesno = scanner.next();
						System.out.println(yesno);
					}while(yesno.equals("Y"));

					targetColumnsBE = targetColumnsBE.replaceAll("\\[", "").replaceAll("\\]","");
					colArrayBE = targetColumnsBE.split(",");
					if(colArrayBE.length > 0 && colArrayBE != null) {
						for(String col : colArrayBE) {
							targetColumnNamesBE.add(col);
						}
					}
				
					//System.out.println(targetColumnNames);
					String expr = input[5].replaceAll("\\[", "").replaceAll("\\]", "");
					String[] parts = expr.split("&");
					for (int i=0;i<parts.length;i++) {
						LeftConst.add( get_Condexpr(outerBE,parts[i].replaceAll("[()]", "")));
						//System.out.println();
					}

					expr = input[6].replaceAll("\\[", "").replaceAll("\\]", "");
					parts = expr.split("&");
					for (int i=0;i<parts.length;i++) {
						RightConst.add( get_Condexpr(innerBE,parts[i].replaceAll("[()]", "")));
						//System.out.println();
					}


					expr = input[7].replaceAll("\\[", "").replaceAll("\\]", "");
					parts = expr.split("&");
					for (int i=0;i<parts.length;i++) {
						EquiConst.add( get_EquiCondexpr(parts[i].replaceAll("[()]", "")));
						//System.out.println();
					}
					if(input[8].contains("-"))
						numBufBE = 0;
					else
						numBufBE = Integer.parseInt(input[8]);

					//----------------------------- Calling BitMap Equi Join
					int numOfColumns1 = outerBE.getNumberOfColumns();
					AttrType[] types1 = outerBE.getAttributeType();
					short[] strSize1 = new short[numOfColumns1];
					int j = 0;
					for (int i = 0; i < numOfColumns1; i++) {
						if (types1[i].attrType == AttrType.attrString) {
							strSize1[j] = (short) 100;
							j++;
						}
					}
					short[] strSizes1 = Arrays.copyOfRange(strSize1, 0, j);

					int numOfColumns2 = innerBE.getNumberOfColumns();
					AttrType[] types2 = innerBE.getAttributeType();
					short[] strSize2 = new short[numOfColumns2];
					j = 0;
					for (int i = 0; i < numOfColumns2; i++) {
						if (types2[i].attrType == AttrType.attrString) {
							strSize2[j] = (short) 100;
							j++;
						}
					}
					short[] strSizes2 = Arrays.copyOfRange(strSize2, 0, j);

					FldSpec[] projlist = new FldSpec[1];
					RelSpec rel = new RelSpec(RelSpec.outer);
					projlist[0] = new FldSpec(rel, 1);

					BitmapEquiJoin BMJ = new BitmapEquiJoin( types1, numOfColumns1,strSizes1,
							types2,numOfColumns2,strSizes2,
							numBufBE,
							outerFile,
							innerFile,
							projlist,1);
					TreeSet<Integer> Leftset = new TreeSet<Integer>();
					TreeSet<Integer> Rightset = new TreeSet<Integer>();
					TreeSet<Integer> set = null;
			
					int count = 0;
					for(CondExpr ex: LeftConst){
						count++;
						set = BMJ.MatchLeftCondition(bitmapMeta, ex);
						if(count ==1) {
							Leftset = set;
						}
						else {
							Leftset.retainAll(set);
						}
					}
					
			
					count = 0;
					for(CondExpr ex: RightConst){
						count++;
						set = BMJ.MatchRightCondition(bitmapMeta, ex);
						if(count == 1) {
							Rightset = set;
						}
						else {
							Rightset.retainAll(set);
						}
					}
					/*for (Integer s : Rightset) {
						System.out.println(s);
					}*/
					
					//TreeSet<String>set1 = null;
					//TreeSet<String>set2 = null;
					//TreeSet<String>set3 = null;
					count = 0;
					for(CondExpr ex: EquiConst) {
						count++;

						switch(count) {
						case 1:	Join1 = new HashMap<>();
						BMJ.MatchingValues(bitmapMeta,Join1,ex,Leftset,Rightset);
						break;
						case 2: Join2 = new HashMap<>();
						BMJ.MatchingValues(bitmapMeta,Join2,ex,Leftset,Rightset);
						break;
						case 3: Join3 = new HashMap<>();
						BMJ.MatchingValues(bitmapMeta,Join3,ex,Leftset,Rightset);
						break;

						}
					}
					Map<List<Integer>,List<Integer>> FinalPos = new HashMap<>();
					for(List<Integer>Left1 : Join1.keySet()) {

						List<Integer>Right1 =  Join1.get(Left1);
						if(Join2 != null) {
							for(List<Integer>Left2: Join2.keySet()) {

								List<Integer>Right2 = Join2.get(Left2);
								if(Join3 != null) {
									for(List<Integer>Left3: Join3.keySet()) {
										List<Integer>Right3 = Join3.get(Left3);

										List<Integer>newleft = new ArrayList<>(Left3);
										newleft.retainAll(Left2);
										newleft.retainAll(Left1);

										List<Integer>newright = new ArrayList<>(Right3);
										newright.retainAll(Right2);
										newright.retainAll(Right1);
										if(newleft.size()>0 && newright.size()>0)
											FinalPos.put(newleft, newright);	
									}
								}
								else {//for join 3 = null
									List<Integer>newleft = new ArrayList<>(Left2);
									newleft.retainAll(Left1);

									List<Integer>newright = new ArrayList<>(Right2);
									newright.retainAll(Right1);
									if(newleft.size()>0 && newright.size()>0)
										FinalPos.put(newleft, newright);	
								}


							}
						}
						else {

							FinalPos = Join1;
						}
					}

					if(FinalPos.size()>0)
						PrintRecords(FinalPos, targetColumnNamesBE,numberOfColumns);
					else
						System.out.println("No Matching Records found!!!");
					
					System.out.println("Join operation completed");
					System.out.println("Number of disk reads: " + ((PCounter.rcounter) - startRead));
					System.out.println("Number of disk writes: " + ((PCounter.wcounter) - startWrite));
					
					SystemDefs.JavabaseBM.resetAllPinCount();
					SystemDefs.JavabaseBM.flushAllPages();
					
				}
				
				
					catch (Exception e) {
						e.printStackTrace();
					}
				System.out.println("**********BITMAP EQUALITY JOIN SUCCESSFULLY COMPLETED**********" );
					


		
		
		
	System.out.println("**********COLUMNAR INDEX SCAN**********" );
		System.out.println("\nEnter the query in this format (query COLUMNDBNAME COLUMNARFILENAME [TARGETCOLUMNNAMES] [VALUECONSTRAINT NUMBUF] ACCESSTYPE");
	//	SystemDefs sysDef = new SystemDefs(columnDbName,100000,100,"Clock");
		
		ColumnarIndexScan cis = new ColumnarIndexScan();
		System.out.println("**********COLUMNAR INDEX SCAN SUCCESSFULLY COMPLETED**********" );
		
		try {

			System.out.println("************COLUMNAR NESTED LOOP JOIN************");
			System.out.println("\nEnter the query in this format (nlj COLUMNDB OUTERFILE INNERFILE OUTERCONST INNERCONST JOINCONST OUTERACCTYPE INNERACCTYPE [TARGETCOLUMNS] NUMBUF");
			in = new BufferedReader(new InputStreamReader(System.in));
			try {
				queryInput = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			input = queryInput.split("\\s+");
			String functionType = input[0];
			//System.out.println(input[0]);
			String columnDB = input[1] ;
			String outerFile = input[2];
			String innerFile = input[3];
			String outerConstraint = input[4];
			String innerConstraint = input[5];
			String joinConstraint = input[6];
			String outerAccessType = input[7];
			String innerAccessType = input[8];
			String targetColumns = input[9];
			int numBuf = Integer.parseInt(input[10]);

			//SystemDefs sysDef = new SystemDefs(columnDB,100000,100,"Clock");		

			ColumnarFile outer = insertRecords(outerFile+".txt"); 
			ColumnarFile inner = insertRecords(innerFile+".txt");

			List<String> targetColumnNames = new ArrayList<String>();
			targetColumns = targetColumns.replaceAll("\\[","").replaceAll("\\]","");
			colArray = targetColumns.split(",");
			if(colArray.length > 0 && colArray != null) {
				for(String col : colArray) {
					targetColumnNames.add(col);
				}
			}
			
			int startRead = 0;
			int startWrite = 0;
			//SystemDefs.JavabaseBM.resetAllPinCount();
			//SystemDefs.JavabaseBM.flushAllPages();
			startRead = PCounter.rcounter;
			startWrite = PCounter.wcounter;

			if (functionType.equals("nlj")) {
				ColumnarNestedLoopsJoins nlj = new ColumnarNestedLoopsJoins(columnDB, outerFile, innerFile);
				List<List<String>> results = nlj.getResults(outer, inner, 
						outerConstraint, innerConstraint, joinConstraint, 
						outerAccessType, innerAccessType, targetColumnNames, numBuf);

				for(List<String> result: results) {
					System.out.println(result);
					//System.out.println("**********COLUMNAR NESTED LOOP JOIN SUCCESSFULLY COMPLETED**********" );
				}
			}

			System.out.println("Join operation completed");
			System.out.println("Number of disk reads: " + ((PCounter.rcounter) - startRead));
			System.out.println("Number of disk writes: " + ((PCounter.wcounter) - startWrite));
			
			SystemDefs.JavabaseBM.resetAllPinCount();
			SystemDefs.JavabaseBM.flushAllPages();
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("**********COLUMNAR NESTED LOOP JOIN SUCCESSFULLY COMPLETED**********" );
		
		
		
		/*System.out.println("**********COLUMNAR SORT**********" );
		System.out.println("\nEnter the query in this format (sort COLUMNDBNAME COLUMNARFILENAME SORTCOLUMN SORTORDER NUMBUF)");
		DBName = input[1];
		String fileName = input[2];
		int sortColumn = getColumnNumber(input[3]);

		ColumnarFile cf = insertRecords("/Users/sucharitharumesh/eclipse-workspace/DBMSI_3/cse510/Sampledata/"+"smalldata_part1.txt"); 
		TupleOrder[] order = new TupleOrder[2];
		order[0] = new TupleOrder(TupleOrder.Ascending);
		order[1] = new TupleOrder(TupleOrder.Descending);

		TupleOrder sortOrder = null;
		if (input[4].equalsIgnoreCase("ASC")) {
			sortOrder = order[0];
		} else {
			sortOrder = order[1];
		}

		int numBuff = Integer.parseInt(input[5]);

		//createDatabaseIfNotExists(columnDBName, numBuff);

		// cf;
		performColumnarSort(cf,fileName, null, sortColumn, sortOrder, numBuff);
		System.out.println("**********COLUMNAR SORT SUCCESSFULLY COMPLETED**********" );*/

		

		
	}

		
		
		public static void PrintRecords(Map<List<Integer>,List<Integer>> Pos, List<String> targetColumnNames,int numcols  ) 
				throws InvalidSlotNumberException, HFException, HFDiskMgrException, HFBufMgrException, Exception {
			int count =0;
			List<Integer>target = new ArrayList<>();
			for(String col : targetColumnNames) {
				target.add(getColumnNumber(col));

			}



			for(List<Integer>Left: Pos.keySet()){

				

				List<List<String>> Leftrec = new ArrayList<>();
				for(Integer posleft: Left) 
					Leftrec.add( fetchResultsFromPosition(outerBE, posleft+1));

				List<Integer>Right = Pos.get(Left);
				List<List<String>> Rightrec = new ArrayList<>();
				for(Integer posright: Right) 
					Rightrec.add( fetchResultsFromPosition(innerBE, posright+1));
				int count1=0, count2=0;
				//COMBINE AND PRINT
				for(List<String>s : Leftrec) {
					for(List<String>s1: Rightrec) {
						count1=0;
						for(String x: s) {
							count1++;
							if(target.contains(count1))
								System.out.print(x+" ");
						}
						count2 = 0;
						for(String x: s1) {
							count2++;
							if(target.contains(count2))
								System.out.print(x+" "); 
						}
						System.out.println();

					}
				}
			}

		}
		
		public static CondExpr get_EquiCondexpr(String value) {
			CondExpr head =null,expr= null,newexpr = null;
			AttrOperator op = new AttrOperator(AttrOperator.aopEQ) ;
			String delims ="[=<>]+";

			String[] str = value.split("[|]");
			for (int i =0 ;i<str.length;i++) {
				String[] parts= str[i].split(delims,2);


				//System.out.println(parts[0]);
				//System.out.println(parts[1]);
				//System.out.println(op.attrOperator);

				newexpr = new CondExpr(AttrType.attrString,AttrType.attrString, parts[0],parts[1],op.attrOperator);

				if(i==0) {
					head = newexpr;
					expr = head;
				}	
				else
				{     expr.set_next(newexpr);
				expr = newexpr;
				}
			}
			return head;
		}

	public static CondExpr get_Condexpr(ColumnarFile cf, String value) {
		CondExpr head =null,expr= null,newexpr = null;
		AttrOperator op = new AttrOperator(AttrOperator.aopEQ) ;
		String delims ="[=<>]+";

		String[] str = value.split("[|]");
		for (int i =0 ;i<str.length;i++) {
			String[] parts= str[i].split(delims,2);



			int ColNo = cf.getColumnNumberFromColumname(parts[0]);
			AttrType typ2 = cf.attributeType[ColNo];

			int x = str[i].indexOf('=');
			int y = str[i].indexOf('<');
			int z = str[i].indexOf('>');

			//System.out.println(x);
			//System.out.println(y);
			//System.out.println(z);

			if(y>0) {
				if(x>0)
					op.attrOperator = AttrOperator.aopLE; 
				else
					op.attrOperator = AttrOperator.aopLT;
			}
			else if(z>0) {
				if(x>0)
					op.attrOperator = AttrOperator.aopGE; 
				else
					op.attrOperator = AttrOperator.aopGT;
			}
			else if(x>0) {
				op.attrOperator = AttrOperator.aopEQ;
			}
			//System.out.println(parts[0]);
			//System.out.println(parts[1]);
			//System.out.println(op.attrOperator);
			if(typ2.attrType == AttrType.attrInteger)
				newexpr = new CondExpr(AttrType.attrString, typ2.attrType, parts[0],Integer.parseInt(parts[1]),op.attrOperator);
			else if(typ2.attrType == AttrType.attrString)
				newexpr = new CondExpr(AttrType.attrString, typ2.attrType, parts[0],parts[1],op.attrOperator);

			if(i==0) {
				head = newexpr;
				expr = head;
			}	
			else
			{     expr.set_next(newexpr);
			expr = newexpr;
			}
		}
		return head;
	}


	public static ColumnarFile insertRecords(String table) {
		
		int startRead = 0;
		int startWrite = 0;
		
		startRead = PCounter.rcounter;
		startWrite = PCounter.wcounter;

		ColumnarFile cf = null;
		String[] columnNames = new String[4];
		AttrType[] columnTypes = new AttrType[4];
		FileInputStream fin;

		int i = 0;
		int tupleLength = 0;
		int stringSize = 0;

		try {
			fin = new FileInputStream(filePath+table);
			DataInputStream din = new DataInputStream(fin);
			BufferedReader bin = new BufferedReader(new InputStreamReader(din));
			String line = bin.readLine();
			StringTokenizer st = new StringTokenizer(line);

			while(st.hasMoreTokens()) {
				String token = st.nextToken();
				StringTokenizer svalue = new StringTokenizer(token);
				String value1 = svalue.nextToken(":");
				String value2 = svalue.nextToken(":");
				columnNames[i] = value1;

				if (value2.equals("int")) {
					columnTypes[i] = new AttrType(AttrType.attrInteger);
					tupleLength = tupleLength + 4;
				} else {
					columnTypes[i] = new AttrType(AttrType.attrString);				
					StringTokenizer t1 = new StringTokenizer(value2);
					t1.nextToken("(");
					String temp = t1.nextToken("(");
					t1 = new StringTokenizer(temp);
					temp = t1.nextToken(")");
					stringSize = Integer.parseInt(temp);
					tupleLength = tupleLength + stringSize;
				}
				i++;
			} 

			cf = new ColumnarFile (table, 4, columnTypes, stringSize);
			cf.columnNames = columnNames;
			cf.setColumnarFileMetadata(stringSize);

			byte [] tupleData = new byte[tupleLength];
			int offset = 0;
			int count = 1;

			while((line = bin.readLine()) != null) {
				StringTokenizer columnValue = new StringTokenizer (line);
				for(AttrType columnType: columnTypes){
					String column = columnValue.nextToken();
					if(columnType.attrType == AttrType.attrInteger) {
						Convert.setIntValue(Integer.parseInt(column), offset, tupleData);
						offset = offset + 4;
					}
					else if (columnType.attrType == AttrType.attrString){
						Convert.setStrValue(column, offset, tupleData);
						offset = offset + stringSize;
					}
				}	
				cf.insertTuple(tupleData);
				//System.out.println("Record Inserted: "+ count);
				offset = 0;
				count++;
				Arrays.fill(tupleData, (byte)0);
			}
			System.out.println(table + " table record inserted: "+ count);
			System.out.println("Number of disk reads: " + ((PCounter.rcounter) ));
			System.out.println("Number of disk writes: " + ((PCounter.wcounter) ));
			
			SystemDefs.JavabaseBM.resetAllPinCount();
			SystemDefs.JavabaseBM.flushAllPages();
			
			bin.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return cf;
	}



	// Batch insert
	public static ColumnarFile batchInsert(String dataFileName, String columnDbName, 
			String columnarFileName, int numberOfColumn) {


		ColumnarFile cf = null;
		// Parameters initialized
		String[] columnNames = new String[numberOfColumn];
		AttrType[] columnTypes = new AttrType[numberOfColumn];

		int i = 0;
		int tupleLength = 0;
		int stringSize = 0;


		@SuppressWarnings("unused")
		SystemDefs sysDef = new SystemDefs(columnDbName,100000,30,"Clock");

		FileInputStream fin;
		try {
			fin = new FileInputStream(filePath+dataFileName);
			DataInputStream din = new DataInputStream(fin);
			BufferedReader bin = new BufferedReader(new InputStreamReader(din));

			startRead = PCounter.rcounter;
			startWrite = PCounter.wcounter;
			//			
			//			System.out.println(startRead + " : " + startWrite);

			String line = bin.readLine();

			StringTokenizer st = new StringTokenizer(line);

			while(st.hasMoreTokens()) {

				String token = st.nextToken();
				StringTokenizer svalue = new StringTokenizer(token);

				String value1 = svalue.nextToken(":");
				String value2 = svalue.nextToken(":");

				//System.out.println(value1 + " " + value2);

				columnNames[i] = value1;

				if (value2.equals("int")) {
					columnTypes[i] = new AttrType(AttrType.attrInteger);
					tupleLength = tupleLength + 4;
				} else {
					columnTypes[i] = new AttrType(AttrType.attrString);				
					StringTokenizer t1 = new StringTokenizer(value2);

					t1.nextToken("(");
					String temp = t1.nextToken("(");
					t1 = new StringTokenizer(temp);
					temp = t1.nextToken(")");
					stringSize = Integer.parseInt(temp);
					tupleLength = tupleLength + stringSize;
				}
				i++;
			} 

			cf = new ColumnarFile (columnarFileName, numberOfColumn, columnTypes, stringSize);
			cf.columnNames = columnNames;
			cf.setColumnarFileMetadata(stringSize);

			byte [] tupleData = new byte[tupleLength];
			int offset = 0;
			int count = 1;

			while((line = bin.readLine()) != null) {

				StringTokenizer columnValue = new StringTokenizer (line);

				for(AttrType columnType: columnTypes){
					String column = columnValue.nextToken();
					if(columnType.attrType == AttrType.attrInteger) {
						Convert.setIntValue(Integer.parseInt(column), offset, tupleData);
						offset = offset + 4;
					}
					else if (columnType.attrType == AttrType.attrString){
						Convert.setStrValue(column, offset, tupleData);
						offset = offset + stringSize;
					}
				}	
				cf.insertTuple(tupleData);
				System.out.println("Record Inserted: "+ count);
				offset = 0;
				count++;
				Arrays.fill(tupleData, (byte)0);

			}
			System.out.println();
			System.out.println("Successfully inserted all the records !!");		
			System.out.println("Number of disk reads: " + (PCounter.rcounter));
			System.out.println("Number of disk writes: " + (PCounter.wcounter));

			System.out.println();

			TupleScan tscan = new TupleScan(cf);
			TID tid = new TID();
			tid.recordIDs = new RID[numberOfColumn];

			Tuple tuple = new Tuple();
			short[] fieldOffset = {0,(short)cf.stringSize,(short)(2*cf.stringSize),(short)(2*cf.stringSize+4)};
			tuple.setTupleMetaData(tupleLength, (short)numberOfColumn, fieldOffset);

			for (i = 0; i < numberOfColumn; i++) {
				tid.recordIDs[i] = new RID();
			}

			while((tuple = tscan.getNext(tid)) != null) {
				System.out.print("Record Fetched: ");
				tuple.print(columnTypes);
			}
			System.out.println();
			System.out.println("Successfully fetched all the records !!");

			System.out.println("Number of disk reads: " + (PCounter.rcounter));
			System.out.println("Number of disk writes: " + (PCounter.wcounter));

			tscan.closetuplescan();	
			bin.close();




		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		try {
			SystemDefs.JavabaseBM.resetAllPinCount();
			SystemDefs.JavabaseBM.flushAllPages();
			//SystemDefs.JavabaseDB.closeDB();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return cf;

	}

	public static void indexQueryBitmap(ColumnarFile cf, String columnDbName, 
			String columnarFileName, String columnName) {

		BitMapFile B = null; 

		String[] columnNames;
		String file_entry_name;

		//ColumnarFileMetadata cfm = cf.getColumnarFileMetadata(columnarFileName+".hdr");
		columnNames = cf.columnNames;
		cf.columnNames = columnNames;

		startRead = PCounter.rcounter;
		startWrite = PCounter.wcounter;

		Heapfile hf = cf.getHeapfileForColumname(columnName);


		try {
			int value;
			String str;
			RID rid = new RID();
			Scan s = hf.openScan();
			Tuple tuple=null;
			System.out.println("Creating BitMap on column: "+columnName);
			int ColNo = cf.getColumnNumberFromColumname(columnName);
			AttrType x = cf.attributeType[ColNo];	

			HashSet<Integer> intHash = new HashSet<>();
			HashSet<String> strHash = new HashSet<>();

			while((tuple=s.getNext(rid))!=null){

				if(x.attrType == AttrType.attrInteger) {
					value = Convert.getIntValue(0, tuple.getData());

					if (!intHash.contains(value)) {
						IntegerValue val = new IntegerValue();
						val.setValue(value);
						intHash.add(value);
						file_entry_name= columnarFileName+Integer.toString(ColNo)+Integer.toString(value);
						//System.out.println(file_entry_name);
						System.out.println("Creating index on: " + value);
						B = new BitMapFile(file_entry_name,cf,ColNo,val);
						//BM.printBitMap(B.getHeaderPage());
					}

				}	
				else if(x.attrType == AttrType.attrString)	{
					str = Convert.getStrValue(0, tuple.getData(),cf.stringSize );					 

					if (!strHash.contains(str)) {
						StringValue vals = new StringValue();
						vals.setValue(str);
						strHash.add(str);
						file_entry_name= columnarFileName+Integer.toString(ColNo)+str;
						//System.out.println(file_entry_name);
						System.out.println("Creating index on: " + str);
						B = new BitMapFile(file_entry_name,cf,ColNo,vals);
						//BM.printBitMap(B.getHeaderPage());
					}
				}

			}
			String name = columnarFileName+columnName;
			if(x.attrType == AttrType.attrInteger)
				bitmapMeta.put(name, intHash);
			else if(x.attrType == AttrType.attrString)
				bitmapMeta.put(name, strHash);
		} catch (Exception e) {
			// TODO: handle exception
		}

		System.out.println("Number of disk reads: " + (PCounter.rcounter-startRead));
		System.out.println("Number of disk writes: " + (PCounter.wcounter-startWrite));


	}


	public static BTreeFile indexQueryBTree(ColumnarFile cf, String columnDbName, 
			String columnarFileName, String columnName, String indexType) {

		BTreeFile bTreeFile = null; 

		String[] columnNames;

		ColumnarFileMetadata cfm = cf.getColumnarFileMetadata(columnarFileName+".hdr");
		columnNames = cfm.columnNames;
		cf.columnNames = columnNames;

		int colNo = cf.getColumnNumberFromColumname(columnName);
		int type = cf.attributeType[colNo].attrType;

		Heapfile hf = cf.getHeapfileForColumname(columnName);

		if(indexType.equalsIgnoreCase("BTREE")) {
			try {

				startRead = PCounter.rcounter;
				startWrite = PCounter.wcounter;

				RID rid = new RID();
				Scan s = hf.openScan();
				Tuple tuple=null;
				System.out.println("Creating BTree on column: "+columnName);
				if (type == 1) {
					bTreeFile = new BTreeFile("btree"+columnName, AttrType.attrInteger, 4, 1);
					while((tuple=s.getNext(rid))!=null){
						int temp=Convert.getIntValue(0, tuple.getData());
						bTreeFile.insert(new btree.IntegerKey(temp), rid);
					}
				}

				else {
					bTreeFile = new BTreeFile("btree"+columnName, AttrType.attrString, 25, 1);
					while((tuple=s.getNext(rid))!=null){
						String temp=Convert.getStrValue(0, tuple.getData(), 25);
						bTreeFile.insert(new btree.StringKey(temp), rid);
					}
				}


			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Number of disk reads: " + (PCounter.rcounter - startRead));
		System.out.println("Number of disk writes: " + (PCounter.wcounter - startWrite));

		try {
			SystemDefs.JavabaseBM.resetAllPinCount();
			SystemDefs.JavabaseBM.flushAllPages();
			//SystemDefs.JavabaseDB.closeDB();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bTreeFile;
	}

	public static void runQuery(String columnDBName, ColumnarFile cf, String columnFileName,
			List<String> columnNames, List<String> valueConstraint, int numBuf, String accessType) {

		try {

			int selectCols[] = new int[columnNames.size()];
			for (int i = 0; i < columnNames.size(); i++) {
				selectCols[i] = getColumnNumber(columnNames.get(i));
			}

			List<String> result = new ArrayList<String>();
			int targetsize = columnNames.size();
			int targetColNo;

			startRead = PCounter.rcounter;
			startWrite = PCounter.wcounter;

			switch(accessType) {

			case "BITMAP":

				int colNo = getColumnNumber(valueConstraint.get(0))-1;
				String val = valueConstraint.get(2);
				String file_entry_name = columnFileName+ Integer.toString(colNo)+val;
				BM BitMap = new BM();
				BitMapFile B = new BitMapFile(file_entry_name);
				int[] positions = BM.getpositions(B.getHeaderPage());
				int size = BM.getCount();				
				for(int i=0;i<size;i++) { 
					//System.out.println("position: "+positions[i]);
					for(int j=0;j<targetsize;j++) {
						targetColNo = getColumnNumber(columnNames.get(j))-1;
						AttrType x = cf.attributeType[targetColNo];	
						if(x.attrType==AttrType.attrInteger)
							System.out.print(HFPage.getIntvalue_forGivenPosition(positions[i],targetColNo , cf));
						else if (x.attrType==AttrType.attrString) 
							System.out.print(HFPage.getStrvalue_forGivenPosition(positions[i],targetColNo , cf));
						System.out.print("  ");
					}
					System.out.println();
				}

				System.out.println("Disk Reads: "+ (PCounter.rcounter-startRead));
				System.out.println("Disk Writes: "+ (PCounter.wcounter-startWrite));
				System.out.println("End of Bitmap query");
				break;

			case "FILESCAN":
				try
				{
					TupleScan ts = new TupleScan(cf);
					Tuple t = new Tuple();

					short[] fieldOffset = {0,(short)cf.stringSize,(short)(2*cf.stringSize),(short)(2*cf.stringSize+4)};
					t.setTupleMetaData(cf.tupleLength, (short)cf.numberOfColumns, fieldOffset);

					TID tid = new TID();
					tid.recordIDs = new RID[cf.numberOfColumns];
					for(int j =0 ; j < cf.numberOfColumns ; j++)
						tid.recordIDs[j] = new RID();

					int columnNumber = cf.getColumnNumberFromColumname(valueConstraint.get(0));
					int type = cf.attributeType[columnNumber].attrType;
					int lowkeyInt=0;
					String lowkeyStr="";
					if(type == 1) {						
						lowkeyInt= Integer.parseInt(valueConstraint.get(2));
					} else {
						lowkeyStr = valueConstraint.get(2);
					}

					char operator = valueConstraint.get(1).charAt(0);

					AttrType[] columnType = cf.attributeType;
					result = new ArrayList<String>();
					while ((t=ts.getNext(tid))!=null) {
						if (type == 1) {
							switch(operator)
							{
							case '>':
								if ( lowkeyInt < (t.getIntFld(columnNumber+1))) {
									result = t.getRecord(columnType);
									for(int j=0;j<targetsize;j++) {
										targetColNo = getColumnNumber(columnNames.get(j))-1;
										System.out.print(result.get(targetColNo) + "      ");
									}
									System.out.println();
								}
								break;
							case '<':
								if (lowkeyInt > (t.getIntFld(columnNumber+1))) {
									result = t.getRecord(columnType);
									for(int j=0;j<targetsize;j++) {
										targetColNo = getColumnNumber(columnNames.get(j))-1;
										System.out.print(result.get(targetColNo) + "      ");
									}
									System.out.println();
								}
								break;
							case '!':
								if (!(lowkeyInt == (t.getIntFld(columnNumber+1)))) {
									result = t.getRecord(columnType);
									for(int j=0;j<targetsize;j++) {
										targetColNo = getColumnNumber(columnNames.get(j))-1;
										System.out.print(result.get(targetColNo) + "      ");
									}
									System.out.println();
								}
								break;
							case '=':
								if (lowkeyInt == (t.getIntFld(columnNumber+1))) {
									result = t.getRecord(columnType);
									for(int j=0;j<targetsize;j++) {
										targetColNo = getColumnNumber(columnNames.get(j))-1;
										System.out.print(result.get(targetColNo) + "      ");
									}
									System.out.println();
								}
								break;

							}
						} else {
							switch(operator)
							{
							case '>':
								if ((lowkeyStr.compareTo(t.getStrFld(columnNumber+1)) < 0)) {
									result = t.getRecord(columnType);
									for(int j=0;j<targetsize;j++) {
										targetColNo = getColumnNumber(columnNames.get(j))-1;
										System.out.print(result.get(targetColNo) + "      ");
									}
									System.out.println();
								}
								break;
							case '<':
								if ((lowkeyStr.compareTo(t.getStrFld(columnNumber+1)) > 0)) {
									result = t.getRecord(columnType);
									for(int j=0;j<targetsize;j++) {
										targetColNo = getColumnNumber(columnNames.get(j))-1;
										System.out.print(result.get(targetColNo) + "      ");
									}
									System.out.println();
								}
								break;
							case '!':
								if (!(lowkeyStr.compareTo(t.getStrFld(columnNumber+1)) == 0)) {
									result = t.getRecord(columnType);
									for(int j=0;j<targetsize;j++) {
										targetColNo = getColumnNumber(columnNames.get(j))-1;
										System.out.print(result.get(targetColNo) + "      ");
									}
									System.out.println();
								}
								break;
							case '=':
								if ((lowkeyStr.compareTo(t.getStrFld(columnNumber+1)) == 0)) {
									result = t.getRecord(columnType);
									for(int j=0;j<targetsize;j++) {
										targetColNo = getColumnNumber(columnNames.get(j))-1;
										System.out.print(result.get(targetColNo) + "      ");
									}
									System.out.println();
								}
								break;

							}

						}		
					}
				}

				catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println("Disk Reads: "+ (PCounter.rcounter-startRead));
				System.out.println("Disk Writes: "+ (PCounter.wcounter-startWrite));
				System.out.println("End of File Scan query.");
				break;

			case "COLUMNSCAN":

				//query COLUMNDBNAME COLUMNARFILENAME [TARGETCOLUMNNAMES] VALUECONSTRAINT NUMBUF ACCESSTYPE
				ColumnarFile columnarFile = cf;
				int numOfColumns = columnarFile.getNumberOfColumns();
				AttrType[] types = columnarFile.getAttributeType();

				AttrType[] attrs = new AttrType[1];
				int columnNumber = getColumnNumber(valueConstraint.get(0)) - 1;
				attrs[0] = types[columnNumber];

				FldSpec[] projlist = new FldSpec[1];
				RelSpec rel = new RelSpec(RelSpec.outer);
				projlist[0] = new FldSpec(rel, 1);
				String filename = columnFileName + String.valueOf(columnNumber);
				int startread = PCounter.rcounter;
				int startwrite = PCounter.wcounter;
				short[] strSize = new short[numOfColumns];
				int j = 0;
				for (int i = 0; i < numOfColumns; i++) {
					if (types[i].attrType == AttrType.attrString) {
						strSize[j] = (short) 100;
						j++;
					}
				}
				short[] strSizes = Arrays.copyOfRange(strSize, 0, j);

				CondExpr[] expr = getValueContraint(valueConstraint);
				int selectedCols[] = new int[columnNames.size()];
				for (int i = 0; i < columnNames.size(); i++) {
					selectedCols[i] = getColumnNumber(columnNames.get(i));
				}

				try {

					ColumnarFileScan columnarFileScan = new ColumnarFileScan(columnFileName, filename, attrs,
							strSizes, (short) 1, 1, selectedCols, projlist, expr, false, cf);
					Tuple tuple;
					while (true) {
						tuple = columnarFileScan.get_next();
						if (tuple == null) {
							break;
						}
						for (int i = 0; i < tuple.noOfFlds(); i++) {
							if (types[selectCols[i] - 1].attrType == AttrType.attrString) {
								System.out.println(tuple.getStrFld(i + 1));
							}
							if (types[selectCols[i] - 1].attrType == AttrType.attrInteger) {
								System.out.println(tuple.getIntFld(i + 1));
							}
							if (types[selectCols[i] - 1].attrType == AttrType.attrReal) {
								System.out.println(tuple.getFloFld(i + 1));
							}
						}
						System.out.println("");
					}
					columnarFileScan.close();
				} catch (Exception e) {
					e.printStackTrace();
				}


				System.out.println("Number of disk reads: " + (PCounter.rcounter-startread));
				System.out.println("Number of disk writes: " + (PCounter.wcounter-startwrite));
				System.out.println("End of Column Scan query");

				break;

			case "BTREE":

				BTreeFile btf = null;
				BTFileScan scan = null;

				String indexColumn = valueConstraint.get(0);
				Heapfile reqHFile=cf.getHeapfileForColumname(indexColumn);

				char operator = valueConstraint.get(1).charAt(0);

				columnNumber = cf.getColumnNumberFromColumname(valueConstraint.get(0));
				int type = cf.attributeType[columnNumber].attrType;
				int lowkeyInt=0, hikeyInt=0;
				String lowkeyStr="", highKeyStr = "";

				if(type == 1) {						
					if (operator == '<') {
						hikeyInt= Integer.parseInt(valueConstraint.get(2));
						lowkeyInt = 0;
					} else {
						lowkeyInt= Integer.parseInt(valueConstraint.get(2));
						hikeyInt = 999999;
					}						
					IntegerKey lowkey=new IntegerKey(lowkeyInt);
					IntegerKey hikey=new IntegerKey(hikeyInt);
					btf = new BTreeFile("btree"+indexColumn, AttrType.attrInteger, 4, 1);
					scan= btf.new_scan(lowkey, hikey);
				} else {	
					if (operator == '<') {
						highKeyStr = valueConstraint.get(2);
						lowkeyStr = "Connecticut";
					} else {
						lowkeyStr = valueConstraint.get(2);
						highKeyStr = "West_Virginia";
					}						

					StringKey lowKey = new StringKey(lowkeyStr);
					StringKey highKey = new StringKey(highKeyStr);
					btf = new BTreeFile("btree"+indexColumn, AttrType.attrString, 25, 1);
					scan= btf.new_scan(lowKey, highKey);					
				}

				KeyDataEntry entry;
				RID rid=new RID();

				try 
				{	
					result = new ArrayList<String>();
					while((entry=scan.get_next())!=null) {
						if(entry!=null) {
							switch(operator)
							{
							case '>':
								if(entry.key instanceof IntegerKey) {
									if(((IntegerKey)entry.key).getKey() > lowkeyInt) {
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										result = getResult(cf, position);
										for(j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + "      ");
										}
										System.out.println();
									}
								}

								else {
									if(((StringKey)entry.key).getKey().compareTo(lowkeyStr)>0){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										result = getResult(cf, position);
										for(j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + "      ");
										}
										System.out.println();
									}
								}								
								break;
							case '<':
								if(entry.key instanceof IntegerKey)	{
									if(((IntegerKey)entry.key).getKey() < hikeyInt){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										result = getResult(cf, position);
										for(j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + "      ");
										}
										System.out.println();
									}
								}
								else {
									if(((StringKey)entry.key).getKey().compareTo(highKeyStr)<0){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										result = getResult(cf, position);
										for(j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + "      ");
										}
										System.out.println();
									}
								}
								break;

							case '=':
								if(entry.key instanceof IntegerKey) {
									if(((IntegerKey)entry.key).getKey() == lowkeyInt){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										result = getResult(cf, position);
										for(j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + "      ");
										}
										System.out.println();
									}
								}
								else {
									if(((StringKey)entry.key).getKey().compareTo(lowkeyStr)==0){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										result = getResult(cf, position);
										for(j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + "      ");
										}
										System.out.println();
									}
								}
								break;
							}
						}
					}
					System.out.println("AT THE END OF BTREE SCAN!");
					System.out.println("Disk Reads: "+ (PCounter.rcounter - startRead));
					System.out.println("Disk Writes: "+ (PCounter.wcounter - startWrite));
				} catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void deleteQuery(String columnDBName, ColumnarFile cf, String columnFileName,
			List<String> valueConstraint, int numBuf, String accessType) {

		try {

			TupleScan ts = new TupleScan(cf);
			Tuple t = new Tuple();

			short[] fieldOffset = {0,(short)cf.stringSize,(short)(2*cf.stringSize),(short)(2*cf.stringSize+4)};
			t.setTupleMetaData(cf.tupleLength, (short)cf.numberOfColumns, fieldOffset);


			TID tid = new TID();
			tid.recordIDs = new RID[cf.numberOfColumns];
			for(int j =0 ; j < cf.numberOfColumns ; j++)
				tid.recordIDs[j] = new RID();

			int columnNumber = cf.getColumnNumberFromColumname(valueConstraint.get(0));
			int type = cf.attributeType[columnNumber].attrType;
			int lowkeyInt=0, hikeyInt=0;
			String lowkeyStr="", highKeyStr = "";

			if(type == 1) {						
				lowkeyInt= Integer.parseInt(valueConstraint.get(2));
			} else {
				lowkeyStr = valueConstraint.get(2);
			}

			char operator = valueConstraint.get(1).charAt(0);

			switch(accessType) {

			case "FILESCAN":
				try
				{
					while ((t=ts.getNext(tid))!=null) {
						if (type == 1) {
							switch(operator)
							{
							case '=':
								if (lowkeyInt == (t.getIntFld(columnNumber+1))) {
									System.out.println(cf.markTupleDeleted(tid));
								}
								break;

							}
						} else {
							switch(operator)
							{
							case '=':
								if ((lowkeyStr.compareTo(t.getStrFld(columnNumber+1)) == 0)) {
									System.out.println(cf.markTupleDeleted(tid));
								}
								break;

							}

						}		
					}
					System.out.println("Total records marked as deleted: " + cf.deleteCount);
				}

				catch (Exception e){
					e.printStackTrace();
				}

				break;

			case "BITMAP":
				String val = valueConstraint.get(2);
				String file_entry_name = columnFileName+ Integer.toString(columnNumber)+val;
				BM b = new BM();
				BitMapFile B = new BitMapFile(file_entry_name);
				int[] positions = BM.getpositions(B.getHeaderPage());
				int size = BM.getCount();
				int i =0;
				int c=0;
				while ((t=ts.getNext(tid))!=null) {
					if(c<positions[i])
						c++;
					else {
						System.out.println(cf.markTupleDeleted(tid));
						c++;
						i++;
					}

					if(i==size){
						break;
					}
				}
				System.out.println(cf.deleteCount);

				B.destroyFile();

				//String columnName = getColumnName(columnNumber); 

				System.out.println("Reindexing...........");

				//for (String colName : bitmapIndex) {

				//					System.out.println(colName);

				//}

				for (String colName : bitmapIndex) {

					DestroyBitmap(cf, columnDBName, columnFileName, colName, "BITMAP");

				}

				for (String colName : bitmapIndex) {

					indexQueryBitmap(cf, columnDBName, columnFileName, colName);

				}

				break;




			case "BTREE":

				BTreeFile btf = null;
				BTFileScan scan = null;

				String indexColumn = valueConstraint.get(0);
				Heapfile reqHFile=cf.getHeapfileForColumname(indexColumn);
				//int columnNumber = cf.getColumnNumberFromColumname(columnFileName);



				btf = new BTreeFile("btree"+indexColumn, AttrType.attrInteger, 4, 1);

				if(type == 1) {						
					if (operator == '<') {
						hikeyInt= Integer.parseInt(valueConstraint.get(2));
						lowkeyInt = 0;
					} else {
						lowkeyInt= Integer.parseInt(valueConstraint.get(2));
						hikeyInt = 999999;
					}						
					IntegerKey lowkey=new IntegerKey(lowkeyInt);
					IntegerKey hikey=new IntegerKey(hikeyInt);
					scan= btf.new_scan(lowkey, hikey);
				} else {	
					if (operator == '<') {
						highKeyStr = valueConstraint.get(2);
						lowkeyStr = "a";
					} else {
						lowkeyStr = valueConstraint.get(2);
						highKeyStr = "zzzzzzzz";
					}						

					StringKey lowKey = new StringKey(lowkeyStr);
					StringKey highKey = new StringKey(highKeyStr);
					scan= btf.new_scan(lowKey, highKey);					
				}

				KeyDataEntry entry;
				RID rid=new RID();

				try 
				{	

					//BT.printBTree(btf.getHeaderPage());
					//System.out.println("printing leaf pages...");
					//BT.printAllLeafPages(btf.getHeaderPage());

					while((entry=scan.get_next())!=null) {
						if(entry!=null) {
							switch(operator)
							{
							case '>':
								if(entry.key instanceof IntegerKey) {
									if(((IntegerKey)entry.key).getKey() > lowkeyInt) {
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										tid = getDeleteTID(cf, position);
										cf.markTupleDeleted(tid);
										/*for(int j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + " ");
										}
										System.out.println();*/
									}
								}

								else {
									if(((StringKey)entry.key).getKey().compareTo(lowkeyStr)>0){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										tid = getDeleteTID(cf, position);
										cf.markTupleDeleted(tid);
										/*for(int j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + " ");
										}
										System.out.println();*/
									}
								}								
								break;
							case '<':
								if(entry.key instanceof IntegerKey)	{
									if(((IntegerKey)entry.key).getKey() < hikeyInt){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										tid = getDeleteTID(cf, position);
										cf.markTupleDeleted(tid);
										/*for(int j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + " ");
										}
										System.out.println();*/
									}
								}
								else {
									if(((StringKey)entry.key).getKey().compareTo(lowkeyStr)<0){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										tid = getDeleteTID(cf, position);
										cf.markTupleDeleted(tid);
										/*for(int j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + " ");
										}
										System.out.println();*/
									}
								}
								break;

							case '=':
								if(entry.key instanceof IntegerKey) {
									if(((IntegerKey)entry.key).getKey() == lowkeyInt){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										tid = getDeleteTID(cf, position);
										cf.markTupleDeleted(tid);
										/*for(int j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + " ");
										}
										System.out.println();*/
									}
								}
								else {
									if(((StringKey)entry.key).getKey().compareTo(lowkeyStr)==0){
										rid=((btree.LeafData)entry.data).getData();
										int position=reqHFile.RidToPos(rid);										
										tid = getDeleteTID(cf, position);
										cf.markTupleDeleted(tid);
										/*for(int j=0;j<targetsize;j++) {
											targetColNo = getColumnNumber(columnNames.get(j))-1;
											System.out.print(result.get(targetColNo) + " ");
										}
										System.out.println();*/
									}
								}
								break;
							}
						}
					}
					System.out.println("");
					System.out.println("Disk Reads: "+ (PCounter.rcounter - startRead));
					System.out.println("Disk Writes: "+ (PCounter.wcounter - startWrite));
				} catch (Exception e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				break;

			case "COLUMNSCAN":
				ColumnarFile columnarFile = cf;
				int numOfColumns = columnarFile.getNumberOfColumns();
				AttrType[] types = columnarFile.getAttributeType();

				AttrType[] attrs = new AttrType[1];
				columnNumber = getColumnNumber(valueConstraint.get(0)) - 1;
				attrs[0] = types[columnNumber];

				FldSpec[] projlist = new FldSpec[1];
				RelSpec rel = new RelSpec(RelSpec.outer);
				projlist[0] = new FldSpec(rel, 1);

				String filename = columnFileName + String.valueOf(columnNumber);
				List<TID> tids = new ArrayList<>();
				short[] strSize = new short[numOfColumns];
				int j = 0;
				for (i = 0; i < numOfColumns; i++) {
					if (types[i].attrType == AttrType.attrString) {
						strSize[j] = (short) 100;
						j++;
					}
				}
				short[] strSizes = Arrays.copyOfRange(strSize, 0, j);

				CondExpr[] expr = getValueContraint(valueConstraint);
				List<String> columnNames = new ArrayList<String>();
				columnNames.add("A");
				columnNames.add("B");
				columnNames.add("C");
				columnNames.add("D");
				int selectedCols[] = new int[columnNames.size()];
				for (i = 0; i < columnNames.size(); i++) {
					selectedCols[i] = getColumnNumber(columnNames.get(i));
				}
				//int startread = PCounter.rcounter;
				//int startwrite = PCounter.wcounter;
				try {

					ColumnarFileScan columnarFileScan = new ColumnarFileScan(columnFileName, filename, attrs,
							strSizes, (short) 1, 1, selectedCols, projlist, expr, false, cf);
					while (true) {
						tid = columnarFileScan.get_all_rid_as_tid();
						if (tid == null) {
							break;
						}
						tids.add(tid);
					}
					columnarFileScan.close();
					for(TID tid1: tids) System.out.println(cf.markTupleDeleted(tid1));

				} catch (Exception e) {
					e.printStackTrace();
				}

				//System.out.println("Number of disk reads: " + (PCounter.rcounter-startread));
				//System.out.println("Number of disk writes: " + (PCounter.wcounter-startwrite));

				break;
			}

			System.out.println("Deleted Records");
			System.out.println();
			//cf.showDeleteDump();

			System.out.println("Purge all (true/false)?");
			boolean isPurge = Boolean.parseBoolean(scanner.next());

			if (isPurge) {
				System.out.println("Purging all the deleted records");
				cf.purgeAllDeletedTuples();
				//cf.showDeleteDump();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> getResult(ColumnarFile cf, int position) throws InvalidSlotNumberException, HFException, HFDiskMgrException, HFBufMgrException, Exception
	{
		int colSize = cf.columnNames.length;
		ArrayList<Tuple> arrTuples=new ArrayList<Tuple>();
		RID rid = new RID();
		Heapfile hf;

		Tuple tuple = new Tuple();
		short[] fieldOffset = {0,(short)cf.stringSize,(short)(2*cf.stringSize),(short)(2*cf.stringSize+4)};
		tuple.setTupleMetaData(cf.tupleLength, (short)cf.numberOfColumns, fieldOffset);

		List<String> result = new ArrayList<String>();
		int val;
		String sval;

		TID tid=new TID(colSize);
		tid.recordIDs=new RID[colSize];
		tid.position=position;
		tid.numRIDs=colSize;
		for(int j=0;j<colSize;j++)
			tid.recordIDs[j]=new RID();
		for (int i = 0 ; i < colSize ; i++)
		{
			hf = cf.getHeapfileForColumname(cf.columnNames[i].toString());
			rid = hf.PosToRid(position);
			tuple = hf.getRecord(rid);
			tid.recordIDs[i]=rid;
			if(tuple.getLength()>4)	{
				sval = Convert.getStrValue(0, tuple.getData(), tuple.getLength());
				result.add(sval);
			}
			else {
				val = Convert.getIntValue(0, tuple.getData());
				result.add(Integer.toString(val));
			}

			arrTuples.add(hf.getRecord(rid));
		}
		return result;
	}

	public static TID getDeleteTID(ColumnarFile cf, int position) throws InvalidSlotNumberException, HFException, HFDiskMgrException, HFBufMgrException, Exception
	{
		int colSize = cf.columnNames.length;
		ArrayList<Tuple> arrTuples=new ArrayList<Tuple>();
		RID rid = new RID();
		Heapfile hf;
		Tuple t = new Tuple();

		TID tid=new TID(colSize);
		tid.recordIDs=new RID[colSize];
		tid.position=position;
		tid.numRIDs=colSize;
		for(int j=0;j<colSize;j++)
			tid.recordIDs[j]=new RID();

		System.out.print("[");
		for (int i = 0 ; i < colSize ; i++)
		{
			hf = cf.getHeapfileForColumname(cf.columnNames[i].toString());
			rid = hf.PosToRid(position);
			t = hf.getRecord(rid);
			short[] fieldOffset = {0,(short)cf.stringSize,(short)(2*cf.stringSize),(short)(2*cf.stringSize+4)};
			t.setTupleMetaData(cf.tupleLength, (short)cf.numberOfColumns, fieldOffset);

			tid.recordIDs[i]=rid;
			if(t.getData().length>4)
				System.out.print(Convert.getStrValue(0, t.getData(), t.getData().length)+", ");
			else
				System.out.print(Convert.getIntValue(0, t.getData())+", ");
			arrTuples.add(hf.getRecord(rid));
		}
		System.out.println("]");
		return tid;
	}

	private static void performColumnarSort(
			ColumnarFile cf,
			String columnarFileName,
			int[] selectedCols,
			int sort_fld,
			TupleOrder sort_order,
			int n_pages)
			throws Exception {


		try {

			ColumnarFile columnarfile = cf;
			
			//int[] a;

			ColumnarSort columnSort = new ColumnarSort(cf, null, sort_fld, sort_order, n_pages);

			AttrType[] types = columnarfile.getAttributeType();
			
			Tuple sortTuple = new Tuple();

			short[] fieldOffset = {0,(short)cf.stringSize,(short)(2*cf.stringSize),(short)(2*cf.stringSize+4)};
			sortTuple.setTupleMetaData(cf.tupleLength, (short)cf.numberOfColumns, fieldOffset);


			sortTuple = columnSort.get_next();

			while (sortTuple != null) {

				//Print the tuple
				sortTuple.initHeaders();
				for (int i = 0; i < sortTuple.noOfFlds(); i++) {
					if (types[i].attrType == AttrType.attrString) {
						System.out.println(sortTuple.getStrFld(i + 1));
					}
					if (types[i].attrType == AttrType.attrInteger) {
						System.out.println(sortTuple.getIntFld(i + 1));
					}
					if (types[i].attrType == AttrType.attrReal) {
						System.out.println(sortTuple.getFloFld(i + 1));
					}
				}

				sortTuple = columnSort.get_next();
			}
		}
		catch (Exception ex)
		{
			System.out.println("Exception in the column sort");
			ex.printStackTrace();

			throw ex;
		}
	}


	public static int getColumnNumber(String columnName){

		int column = 1;
		switch(columnName){
		case "A":
			column = 1;
			break;

		case "B":
			column = 2;
			break;

		case "C":
			column = 3;
			break;

		case "D":
			column = 4;
			break;
		}
		return column;
	}

	public static int getOp(String op) {

		if (op.equalsIgnoreCase("="))
			return AttrOperator.aopEQ;
		else if (op.equalsIgnoreCase("<"))
			return AttrOperator.aopLT;
		else if (op.equalsIgnoreCase(">"))
			return AttrOperator.aopGT;
		else if (op.equalsIgnoreCase("!="))
			return AttrOperator.aopNE;
		else if (op.equalsIgnoreCase(">="))
			return AttrOperator.aopGE;
		else
			return AttrOperator.aopLE;
	}

	public static CondExpr[] getValueContraint(List<String> valueContraint) {
		if (valueContraint.isEmpty())
			return null;

		int operator = getOp(valueContraint.get(1));
		int column = getColumnNumber(valueContraint.get(0));

		CondExpr[] expr = new CondExpr[2];
		expr[0] = new CondExpr();
		expr[0].op = new AttrOperator(operator);
		expr[0].type1 = new AttrType(AttrType.attrSymbol);
		expr[0].operand1.symbol = new FldSpec(new RelSpec(RelSpec.outer), 1);
		expr[0].next = null;

		String value = valueContraint.get(2);
		if (value.matches("\\d*\\.\\d*")) {
			expr[0].type2 = new AttrType(AttrType.attrReal);
			expr[0].operand2.real = Float.valueOf(value);
		} else if (value.matches("\\d+")) {
			expr[0].type2 = new AttrType(AttrType.attrInteger);
			expr[0].operand2.integer = Integer.valueOf(value);
		} else {
			expr[0].type2 = new AttrType(AttrType.attrString);
			expr[0].operand2.string = value;
		}
		expr[1] = null;
		return expr;
	}

	public static void DestroyBitmap(ColumnarFile cf, String columnDbName, 

			String columnarFileName, String columnName, String indexType) {


		BitMapFile B = null; 


		String[] columnNames;

		String file_entry_name;


		ColumnarFileMetadata cfm = cf.getColumnarFileMetadata(columnarFileName+".hdr");

		columnNames = cfm.columnNames;

		cf.columnNames = columnNames;


		int startread, startwrite;

		startread = PCounter.rcounter;

		startwrite = PCounter.wcounter;


		Heapfile hf = cf.getHeapfileForColumname(columnName);


		if(indexType.equalsIgnoreCase("BITMAP")) {

			try {

				int value;

				String str;

				RID rid = new RID();

				Scan s = hf.openScan();

				Tuple tuple=null;

				System.out.println("Deleting BitMap on column: "+columnName);

				int ColNo = cf.getColumnNumberFromColumname(columnName);

				AttrType x = cf.attributeType[ColNo];	


				HashSet<Integer> intHash = new HashSet<>();

				HashSet<String> strHash = new HashSet<>();


				while((tuple=s.getNext(rid))!=null){


					if(x.attrType == AttrType.attrInteger) {

						value = Convert.getIntValue(0, tuple.getData());


						if (!intHash.contains(value)) {

							IntegerValue val = new IntegerValue();

							val.setValue(value);

							intHash.add(value);

							file_entry_name= columnarFileName+Integer.toString(ColNo)+Integer.toString(value);

							//System.out.println(file_entry_name);

							System.out.println("Deleting index on: " + value);

							B = new BitMapFile(file_entry_name);

							B.destroyFile();

							//BM.printBitMap(B.getHeaderPage());

						}


					}	

					else if(x.attrType == AttrType.attrString)	{

						str = Convert.getStrValue(0, tuple.getData(),cf.stringSize );					 


						if (!strHash.contains(str)) {

							StringValue vals = new StringValue();

							vals.setValue(str);

							strHash.add(str);

							file_entry_name= columnarFileName+Integer.toString(ColNo)+str;

							//System.out.println(file_entry_name);

							System.out.println("Deleting index on: " + str);

							B = new BitMapFile(file_entry_name);

							B.destroyFile();

							//BM.printBitMap(B.getHeaderPage());

						}

					}


				}

			} catch (Exception e) {

				// TODO: handle exception

			}


			//System.out.println("Number of disk reads: " + (PCounter.rcounter-startread));

			//System.out.println("Number of disk writes: " + (PCounter.wcounter-startwrite));

		}


	}

	//return columnname from given column number

	public static String getColumnName(int columnNum){


		String columnname = "A";

		switch(columnNum){

		case 0:

			columnname = "A";

			break;


		case 1:

			columnname = "B";

			break;


		case 2:

			columnname = "C";

			break;


		case 3:

			columnname = "D";

			break;

		}
		return columnname;

	}
	public static List<String> fetchResultsFromPosition(ColumnarFile cf, int position) throws InvalidSlotNumberException, HFException, HFDiskMgrException, HFBufMgrException, Exception {

		int colSize = cf.columnNames.length;
		ArrayList<Tuple> arrTuples=new ArrayList<Tuple>();
		RID rid = new RID();
		Heapfile hf;

		Tuple tuple = new Tuple();
		short[] fieldOffset = {0,(short)cf.stringSize,(short)(2*cf.stringSize),(short)(2*cf.stringSize+4)};
		tuple.setTupleMetaData(cf.tupleLength, (short)cf.numberOfColumns, fieldOffset);

		List<String> result = new ArrayList<String>();
		int val;
		String sval;

		TID tid=new TID(colSize);
		tid.recordIDs=new RID[colSize];
		tid.position=position;
		tid.numRIDs=colSize;

		for(int j=0;j<colSize;j++)
			tid.recordIDs[j]=new RID();

		for (int i = 0 ; i < colSize ; i++){
			hf = cf.getHeapfileForColumname(cf.columnNames[i].toString());
			rid = hf.PosToRid(position);
			tuple = hf.getRecord(rid);
			tid.recordIDs[i]=rid;
			if(tuple.getLength()>4)	{
				sval = Convert.getStrValue(0, tuple.getData(), tuple.getLength());
				result.add(sval);
			}
			else {
				val = Convert.getIntValue(0, tuple.getData());
				result.add(Integer.toString(val));
			}
			arrTuples.add(hf.getRecord(rid));
		}
		return result;
	}	

	static class GetStuff {
		GetStuff() {}

		public static int getChoice () {

			BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
			int choice = -1;

			try {
				choice = Integer.parseInt(in.readLine());
			}
			catch (NumberFormatException e) {
				return -1;
			}
			catch (IOException e) {
				return -1;
			}

			return choice;
		} 

		public static String getReturn () {

			BufferedReader in = new BufferedReader (new InputStreamReader(System.in));
			String ret=null;
			try {
				ret = in.readLine();
			}
			catch (IOException e) {}
			return ret;
		} 

	}

}