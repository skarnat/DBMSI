package bitmap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import bitmap.BM;
import bitmap.BitMapFile;
import btree.ConstructPageException;
import btree.GetFileEntryException;
import btree.PinPageException;
import global.AttrOperator;
import global.AttrType;
import heap.HFPage;
import iterator.CondExpr;
import iterator.FldSpec;

public class BitmapEquiJoin {

	AttrType[] in1; // Attribute types in outerfile
	int len_in1; //number of columns
	short[] t1_str_sizes; // size of columns

	AttrType[] in2; //Attribute types in innerfile
	int len_in2; //number of columns
	short[] t2_str_sizes; //size of columns

	int amt_of_mem;//buffersize

	String leftColumnarFileName;


	String rightColumnarFileName;





	FldSpec[] proj_list;
	int n_out_flds;

	int[] positions_left; //postions that match left condExpr
	int[] positions_right; //positions that match right condExpr

	int right_size,left_size;

	TreeSet<Integer> pos_left = new TreeSet<Integer>();
	TreeSet<Integer> pos_right = new TreeSet<Integer>();

	List<Integer> IntList;
	List<String> StrList;

	List<Integer> Left_Positions = new ArrayList<Integer>() ; //all final positions from Leftcolumnarfile
	List<Integer> Right_Positions = new ArrayList<Integer>(); //all final positions from Rightcolumnarfile

	TreeSet<Integer> Final_Left = new TreeSet<Integer>();
	TreeSet<Integer> Final_Right = new TreeSet<Integer>();
	
	public BitmapEquiJoin(AttrType[] input1, int length_in1,short[] t1_sizes,
			AttrType[] input2,int length_in2,short[] t2_sizes,int numbuf,
			String leftFileName,
			String righFileName,

			FldSpec[] list,int n_out) {
		in1 = input1;
		len_in1 = length_in1;
		t1_str_sizes = t1_sizes;

		in2 = input2;
		len_in2 =length_in2;
		t2_str_sizes = t2_sizes;

		amt_of_mem = numbuf;

		leftColumnarFileName = leftFileName;

		rightColumnarFileName = righFileName;


		proj_list = list;
		n_out_flds = n_out;

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

	public TreeSet<Integer> MatchLeftCondition(Map bitmapMeta, CondExpr Left) 
			throws GetFileEntryException, 
			PinPageException, 
			ConstructPageException, 
			IOException {

		pos_left = new TreeSet<Integer>();
		do{
			int colnum = getColumnNumber(Left.operand1.string)-1;
			String key = leftColumnarFileName + Left.operand1.string;
			String file_entry_name="";

			//Equality
			if(Left.op.attrOperator == AttrOperator.aopEQ) {

				//Getfilename
				if(Left.type2.attrType == AttrType.attrInteger)
					file_entry_name = leftColumnarFileName + Integer.toString(colnum)+Integer.toString(Left.operand2.integer);
				else if ( Left.type2.attrType == AttrType.attrString ) 
					file_entry_name = leftColumnarFileName + Integer.toString(colnum)+Left.operand2.string;

				//Open bitmap file
				BM BitMap = new BM();
				//System.out.println(file_entry_name);
				BitMapFile B = new BitMapFile(file_entry_name);

				//get positions
				positions_left = BM.getpositions(B.getHeaderPage());
				left_size = BM.getCount();		
				for(int i=0; i<left_size;i++){
					//System.out.println(positions_left[i]);
					if(!pos_left.contains(positions_left[i])) {
						pos_left.add(positions_left[i]);
					}
				}
			}

			//Less than
			else if(Left.op.attrOperator == AttrOperator.aopLT) {

				if(Left.type2.attrType == AttrType.attrInteger) {

					HashSet<Integer>set =  (HashSet<Integer>) bitmapMeta.get(key);

					List<Integer> IntList = new ArrayList <Integer> (set);
					Collections.sort(IntList);

					Integer Match = Left.operand2.integer;
					//find each value less than given value
					for (Integer value : IntList) {

						//open each file and get positions
						if(Match > value) {
							file_entry_name = leftColumnarFileName + Integer.toString(colnum)+Integer.toString(value);

							//Open bitmap file
							BM BitMap = new BM();
							//System.out.println(file_entry_name);
							BitMapFile B = new BitMapFile(file_entry_name);

							//get positions
							positions_left = BM.getpositions(B.getHeaderPage());
							left_size = BM.getCount();		
							for(int i=0; i<left_size;i++){
								//System.out.println(positions_left[i]);
								if(!pos_left.contains(positions_left[i])) {
									pos_left.add(positions_left[i]);
								}
							}

						}
						//	System.out.println("next");
					}

				}
				else if(Left.type2.attrType == AttrType.attrString) {

					HashSet<String>set = (HashSet<String>) bitmapMeta.get(key);

					List<String> StrList = new ArrayList <String> (set);
					Collections.sort(StrList);

					String Match = Left.operand2.string;
					//find each value less than given value
					for(String value : StrList) {

						//open each file and get positions
						if(Match.compareTo(value)>0) {
							file_entry_name = leftColumnarFileName + Integer.toString(colnum)+value;

							//Open bitmap file
							BM BitMap = new BM();
							//System.out.println(file_entry_name);
							BitMapFile B = new BitMapFile(file_entry_name);

							//get positions
							positions_left = BM.getpositions(B.getHeaderPage());
							left_size = BM.getCount();		
							for(int i=0; i<left_size;i++){
								//System.out.println(positions_left[i]);
								if(!pos_left.contains(positions_left[i])) {
									pos_left.add(positions_left[i]);
								}
							}

						}


					}


				}

			}
			//Greater than
			else if(Left.op.attrOperator == AttrOperator.aopGT) {

				if(Left.type2.attrType == AttrType.attrInteger) {

					HashSet<Integer>set =(HashSet<Integer>) bitmapMeta.get(key);

					List<Integer> IntList = new ArrayList <Integer> (set);
					Collections.sort(IntList);

					Integer Match = Left.operand2.integer;
					//find each value less than given value
					for (Integer value : IntList) {

						//open each file and get positions
						if(Match < value) {
							file_entry_name = leftColumnarFileName + Integer.toString(colnum)+Integer.toString(value);

							//Open bitmap file
							BM BitMap = new BM();
							//System.out.println(file_entry_name);
							BitMapFile B = new BitMapFile(file_entry_name);

							//get positions
							positions_left = BM.getpositions(B.getHeaderPage());
							left_size = BM.getCount();		
							for(int i=0; i<left_size;i++){
								//System.out.println(positions_left[i]);
								if(!pos_left.contains(positions_left[i])) {
									pos_left.add(positions_left[i]);
								}
							}

						}
						//System.out.println("next");
					}

				}
				else if(Left.type2.attrType == AttrType.attrString) {

					HashSet<String>set = (HashSet<String>) bitmapMeta.get(key);

					List<String> StrList = new ArrayList <String> (set);
					Collections.sort(StrList);

					String Match = Left.operand2.string;
					//find each value less than given value
					for(String value : StrList) {

						//open each file and get positions
						if(Match.compareTo(value)<0) {
							file_entry_name = leftColumnarFileName + Integer.toString(colnum)+value;

							//Open bitmap file
							BM BitMap = new BM();
							//System.out.println(file_entry_name);
							BitMapFile B = new BitMapFile(file_entry_name);

							//get positions
							positions_left = BM.getpositions(B.getHeaderPage());
							left_size = BM.getCount();		
							for(int i=0; i<left_size;i++){
								//System.out.println(positions_left[i]);
								if(!pos_left.contains(positions_left[i])) {
									pos_left.add(positions_left[i]);
								}
							}

						}


					}


				}

			}
			Left = Left.next;
		}while(Left!=null);
		return pos_left;
	}

	public TreeSet<Integer> MatchRightCondition(Map bitmapMeta, CondExpr Right) 
			throws GetFileEntryException, 
			PinPageException, 
			ConstructPageException, 
			IOException {
		pos_right = new TreeSet<Integer>();
		do{
			int colnum = getColumnNumber(Right.operand1.string)-1;
			String key = rightColumnarFileName + Right.operand1.string;
			String file_entry_name="";

			//Equality
			if(Right.op.attrOperator == AttrOperator.aopEQ) {

				//Getfilename
				if(Right.type2.attrType == AttrType.attrInteger)
					file_entry_name = rightColumnarFileName + Integer.toString(colnum)+Integer.toString(Right.operand2.integer);
				else if ( Right.type2.attrType == AttrType.attrString ) 
					file_entry_name = rightColumnarFileName + Integer.toString(colnum)+Right.operand2.string;

				//Open bitmap file
				BM BitMap = new BM();
				//System.out.println(file_entry_name);
				BitMapFile B = new BitMapFile(file_entry_name);

				//get positions
				positions_right = BM.getpositions(B.getHeaderPage());
				right_size = BM.getCount();		
				for(int i=0; i<right_size;i++){
					//System.out.println(positions_right[i]);
					if(!pos_right.contains(positions_right[i])) {
						pos_right.add(positions_right[i]);
					}
				}
			}

			//Less than
			else if(Right.op.attrOperator == AttrOperator.aopLT) {

				if(Right.type2.attrType == AttrType.attrInteger) {

					HashSet<Integer>set = (HashSet<Integer>) bitmapMeta.get(key);

					List<Integer> IntList = new ArrayList <Integer> (set);
					Collections.sort(IntList);

					Integer Match = Right.operand2.integer;
					//find each value less than given value
					for (Integer value : IntList) {

						//open each file and get positions
						if(Match > value) {
							file_entry_name = rightColumnarFileName + Integer.toString(colnum)+Integer.toString(value);

							//Open bitmap file
							BM BitMap = new BM();
							//System.out.println(file_entry_name);
							BitMapFile B = new BitMapFile(file_entry_name);

							//get positions
							positions_right = BM.getpositions(B.getHeaderPage());
							right_size = BM.getCount();		
							for(int i=0; i<right_size;i++){
								//System.out.println(positions_right[i]);
								if(!pos_right.contains(positions_right[i])) {
									pos_right.add(positions_right[i]);
								}
							}

						}
						//System.out.println("next");
					}

				}
				else if(Right.type2.attrType == AttrType.attrString) {

					HashSet<String>set =  (HashSet<String>) bitmapMeta.get(key);

					List<String> StrList = new ArrayList <String> (set);
					Collections.sort(StrList);

					String Match = Right.operand2.string;
					//find each value less than given value
					for(String value : StrList) {

						//open each file and get positions
						if(Match.compareTo(value)>0) {
							file_entry_name = rightColumnarFileName + Integer.toString(colnum)+value;

							//Open bitmap file
							BM BitMap = new BM();
							//System.out.println(file_entry_name);
							BitMapFile B = new BitMapFile(file_entry_name);

							//get positions
							positions_right = BM.getpositions(B.getHeaderPage());
							right_size = BM.getCount();		
							for(int i=0; i<right_size;i++){
								//System.out.println(positions_right[i]);
								if(!pos_right.contains(positions_right[i])) {
									pos_right.add(positions_right[i]);
								}
							}

						}


					}


				}

			}
			//Greater than
			else if(Right.op.attrOperator == AttrOperator.aopGT) {

				if(Right.type2.attrType == AttrType.attrInteger) {

					HashSet<Integer>set =  (HashSet<Integer>) bitmapMeta.get(key);

					List<Integer> IntList = new ArrayList <Integer> (set);
					Collections.sort(IntList);

					Integer Match = Right.operand2.integer;
					//find each value less than given value
					for (Integer value : IntList) {

						//open each file and get positions
						if(Match < value) {
							file_entry_name = rightColumnarFileName + Integer.toString(colnum)+Integer.toString(value);

							//Open bitmap file
							BM BitMap = new BM();
							//System.out.println(file_entry_name);
							BitMapFile B = new BitMapFile(file_entry_name);

							//get positions
							positions_right = BM.getpositions(B.getHeaderPage());
							right_size = BM.getCount();		
							for(int i=0; i<right_size;i++){
								//System.out.println(positions_right[i]);
								if(!pos_right.contains(positions_right[i])) {
									pos_right.add(positions_right[i]);
								}
							}

						}
						//System.out.println("next");
					}

				}
				else if(Right.type2.attrType == AttrType.attrString) {

					HashSet<String>set =  (HashSet<String>) bitmapMeta.get(key);

					List<String> StrList = new ArrayList <String> (set);
					Collections.sort(StrList);

					String Match =Right.operand2.string;
					//find each value less than given value
					for(String value : StrList) {

						//open each file and get positions
						if(Match.compareTo(value)<0) {
							file_entry_name = rightColumnarFileName + Integer.toString(colnum)+value;

							//Open bitmap file
							BM BitMap = new BM();
							//System.out.println(file_entry_name);
							BitMapFile B = new BitMapFile(file_entry_name);

							//get positions
							positions_right = BM.getpositions(B.getHeaderPage());
							right_size = BM.getCount();		
							for(int i=0; i<right_size;i++){
								//System.out.println(positions_right[i]);
								if(!pos_right.contains(positions_right[i])) {
									pos_right.add(positions_right[i]);
								}
							}

						}


					}


				}

			}
			Right = Right.next;
		}while(Right!=null);
		return pos_right;
	}

	@SuppressWarnings("unchecked")
	public void MatchingValues(Map bitmapMeta, Map Join,CondExpr Equi,TreeSet<Integer> Left, TreeSet<Integer>Right) 
			throws GetFileEntryException, 
			PinPageException, 
			ConstructPageException, 
			IOException {
		do {
			int leftJoinField = getColumnNumber(Equi.operand1.string)-1;
			int rightJoinField = getColumnNumber(Equi.operand2.string)-1;
			
			String key1 = leftColumnarFileName + Equi.operand1.string;
			String key2 = rightColumnarFileName + Equi.operand2.string;
			String filename;
			BM BitMap;
			BitMapFile B;
			int[] positions;
			int size;

			if(in1[leftJoinField].attrType == AttrType.attrInteger) {

				//System.out.println(bitmapMeta.get(key1));
				//System.out.println(bitmapMeta.get(key2));
				HashSet<Integer>set1 =  (HashSet<Integer>) bitmapMeta.get(key1);
				HashSet<Integer>set2 = (HashSet<Integer>) bitmapMeta.get(key2);

				set1.retainAll(set2);
				IntList = new ArrayList <Integer> (set1);


				for(Integer value : IntList) { // for each matching value
					Left_Positions = new ArrayList<Integer>() ; 
					Right_Positions = new ArrayList<Integer>();

					//left file positions
					filename = leftColumnarFileName+Integer.toString(leftJoinField) + Integer.toString(value);
					BitMap = new BM();
					B = new BitMapFile(filename);
					positions = BM.getpositions(B.getHeaderPage());
					size = BM.getCount();
					for(int i=0; i<size;i++) {
						if(Left.contains(positions[i])) {
							Left_Positions.add(positions[i]);
						}
					}

					//right file positions
					filename = rightColumnarFileName+Integer.toString(rightJoinField)+ Integer.toString(value);
					BitMap = new BM();
					B = new BitMapFile(filename);
					positions = BM.getpositions(B.getHeaderPage());
					size = BM.getCount();
					for(int i=0; i<size;i++) {
						if(Right.contains(positions[i])) {
							Right_Positions.add(positions[i]);
						}
					}
					if(Left_Positions.size()>0 && Right_Positions.size()>0)
					{	//System.out.println();
					//System.out.println(value);
					//print();
					
					Join.put(Left_Positions,Right_Positions);
					
					}	
				}
			}
			else if(in1[leftJoinField].attrType == AttrType.attrString) {


				HashSet<String>set1 = (HashSet<String>) bitmapMeta.get(key1);
				HashSet<String>set2 = (HashSet<String>) bitmapMeta.get(key2);

				set1.retainAll(set2);
				StrList = new ArrayList <String> (set1);

				for(String value : StrList) { // for each matching value
					Left_Positions = new ArrayList<Integer>() ; 
					Right_Positions = new ArrayList<Integer>();

					//left file positions
					filename  = leftColumnarFileName+Integer.toString(leftJoinField) + value;
					BitMap = new BM();
					B = new BitMapFile(filename);
					positions = BM.getpositions(B.getHeaderPage());
					size = BM.getCount();
					for(int i=0; i<size;i++) {
						if(Left.contains(positions[i])) {
							Left_Positions.add(positions[i]);
						}
					}

					//right file positions
					filename = rightColumnarFileName+Integer.toString(rightJoinField)+ value;
					BitMap = new BM();
					B = new BitMapFile(filename);
					positions = BM.getpositions(B.getHeaderPage());
					size = BM.getCount();
					for(int i=0; i<size;i++) {
						if(Right.contains(positions[i])) {
							Right_Positions.add(positions[i]);
						}
					}
					if(Left_Positions.size()>0 && Right_Positions.size()>0)
					{	//System.out.println();
					//System.out.println(value);
					//print();
					Join.put(Left_Positions,Right_Positions);
					}	
				}

			}
			Equi = Equi.next;
		}while(Equi!=null);
	}

	/*public void print() {
		Collections.sort(Left_Positions);
		Collections.sort(Right_Positions);
		//System.out.println("Leftpos ");
		for (int i =0 ; i<Left_Positions.size();i++)
			//System.out.println(Left_Positions.get(i));
		//System.out.println("Rightpos ");
		for (int i =0 ; i<Right_Positions.size();i++)
		//	System.out.println(Right_Positions.get(i));
		}*/

	/*public void Equijoin() {



		AttrType left = in1[leftJoinField]	;
		AttrType right = in2[rightJoinField];
		if(left.attrType != right.attrType )
			System.out.println("Types dont match!");
		else {
			Integer leftint;
			String Leftstr;
			for (int pos: pos_left) {
				if(left.attrType==AttrType.attrInteger)
				{	leftint = HFPage.getIntvalue_forGivenPosition(positions[i],targetColNo , ));



				}	
					else if (x.attrType==AttrType.attrString) 
						System.out.print(HFPage.getStrvalue_forGivenPosition(positions[i],targetColNo , cf));
		}
	}(*/


}
