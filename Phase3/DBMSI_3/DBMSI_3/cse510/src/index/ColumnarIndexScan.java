package index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import bitmap.BitMapFile;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;


import btree.BTFileScan;
import btree.BTreeFile;
import btree.IntegerKey;
import btree.KeyDataEntry;
import btree.StringKey;
import bufmgr.PageNotReadException;
import columnar.ColumnarFile;
import columnar.ColumnarFileMetadata;
import diskmgr.PCounter;
import global.AttrType;
import global.Convert;
import global.RID;
import global.SystemDefs;
import global.TID;
import heap.HFBufMgrException;
import heap.HFDiskMgrException;
import heap.HFException;
import heap.HFPage;
import heap.Heapfile;
import heap.InvalidSlotNumberException;
import heap.InvalidTupleSizeException;
import heap.InvalidTypeException;
import heap.Scan;
import heap.Tuple;
import iterator.Iterator;
import iterator.JoinsException;
import iterator.LowMemException;
import iterator.PredEvalException;
import iterator.SortException;
import iterator.TupleUtilsException;
import iterator.UnknowAttrType;
import iterator.UnknownKeyTypeException;
import value.IntegerValue;
import value.StringValue;

public class ColumnarIndexScan extends Iterator {

//	String _relName;
//	int[] _fldNum;
//	IndexType[] _index;
//	String[] _indName;
//	private Heapfile f;
//	private Scan scan;
//	private Tuple Jtuple;
//	AttrType[] _types;
//	short[] string_sizes;
//	private int[] _outputColumnsIndexes;
//	int _noInFlds;
//	int _nOutFlds;
//	FldSpec[] perm_mat;
//	CondExpr[] _selects;
//	boolean _indexOnly;
//	private IndexFile[] indexFile = null;
//	private BitMapFile[] bmf=null;
//	private BTreeFile[] btf=null;
//	private Scan bitMapScan=null;
//	private IndexFileScan[] indScan;
//	private String _columnFileName;
	
	public ColumnarIndexScan() {

	String queryInput = null;
	BufferedReader in;
	String[] input = null;
	String DBName ="";
	String filename ="";
	String valueconstraint= "";
	String accesstype="";
	
	List<List<String>> resultTuples = new ArrayList<>();
	//1.Tuples has to be inserted already and read in a columnarfile cf
	//2. retrieve tuples - COLUMNARFILENAME [(VALUECONSTRAINT)] ACCESSTYPE
	in = new BufferedReader(new InputStreamReader(System.in));
	try {
		queryInput = in.readLine();
	} catch (IOException e) {
		e.printStackTrace();
	}
	input = queryInput.split("\\s+");
	
	//DBName = input[1];
	filename = input[1];
	valueconstraint = input[2];
	accesstype = input[3];
	System.out.println(filename);
	ColumnarFile cf = insertRecords(filename+".txt"); 
	System.out.println("working");
	resultTuples = getFinalTuples(cf,valueconstraint,accesstype);
	System.out.println(resultTuples.toString());
	
	}
	
	public List<List<String>> getFinalTuples(ColumnarFile cf,String valueconstraint,String accesstype){
		
		
		List<String> indexType=new ArrayList();
		indexType = Arrays.asList(accesstype.split(","));
		int i=0;
		List<String>conjuncts = new ArrayList();
		List<List<String>> temp = new ArrayList<>();
		List<List<String>> resultTuples = new ArrayList<>();
		
		System.out.println(indexType.get(i));
		
		valueconstraint = valueconstraint.replaceAll("\\[","").replaceAll("\\]","");
		if(valueconstraint.contains("&")){
				   conjuncts = Arrays.asList(valueconstraint.split("&"));
				} else {
					conjuncts.add(valueconstraint);
				}
				
		for(String conjunct : conjuncts) {
			//List<String> disjuncts = new ArrayList<String>();
			if(!conjunct.contains("|")){
				List<String> andcond = getConditionList(conjunct);
				if(indexType.get(i).equalsIgnoreCase("BTREE"))
					temp = getResultsofBtree(cf,andcond, indexType.get(i),"file");
				else 
					temp = getResultsofBitmap(cf,andcond, indexType.get(i));
				
				if(resultTuples.size() == 0)
					resultTuples.addAll(temp);
				else
					resultTuples.retainAll(temp);
								//System.out.println(outerResults.toString());
			} else{
				conjunct = conjunct.replaceAll("\\(","").replaceAll("\\)","");
			    String[] disjuncts = conjunct.split("\\|");
				for(String disjuct : disjuncts) {
				List<String> disjunctcond = getConditionList(disjuct);
				if(indexType.get(i).equalsIgnoreCase("BTREE"))
					temp = getResultsofBtree(cf,disjunctcond, indexType.get(i),"file");
				else 
					temp = getResultsofBitmap(cf,disjunctcond, indexType.get(i));
				
				resultTuples.addAll(temp);
			}
				Set<List<String>> hs = new HashSet<>();
				hs.addAll(resultTuples);
				resultTuples.clear();
				resultTuples.addAll(hs);
								//System.out.println(outerResults.toString());
			}
		}
	
		return resultTuples;
	}
	
	public List<List<String>> getResultsofBtree(ColumnarFile cf, List<String> valueConstraint, String accessType,String columnarFileName) {

		List<List<String>> returnResult = new ArrayList<>();
		List<String> result = new ArrayList<String>();

		try {
			BTreeFile btf = null;
			BTFileScan scan = null;

			String indexColumn = valueConstraint.get(0);
			Heapfile reqHFile=cf.getHeapfileForColumname(indexColumn);

			char operator = valueConstraint.get(1).charAt(0);

			int columnNumber = cf.getColumnNumberFromColumname(valueConstraint.get(0));
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
				createBtreeIndex(cf, "column", columnarFileName, indexColumn);
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
				createBtreeIndex(cf, "column", columnarFileName, indexColumn);
				btf = new BTreeFile("btree"+indexColumn, AttrType.attrString, 25, 1);
				scan= btf.new_scan(lowKey, highKey);					
			}

			KeyDataEntry entry;
			RID rid=new RID();

			while((entry=scan.get_next())!=null) {
				if(entry!=null) {
					switch(operator)
					{
					case '>':
						if(entry.key instanceof IntegerKey) {
							if(((IntegerKey)entry.key).getKey() > lowkeyInt) {
								rid=((btree.LeafData)entry.data).getData();
								int position=reqHFile.RidToPos(rid);										
								result = fetchResultsFromPosition(cf, position);
								returnResult.add(result);
							}
						}

						else {
							if(((StringKey)entry.key).getKey().compareTo(lowkeyStr)>0){
								rid=((btree.LeafData)entry.data).getData();
								int position=reqHFile.RidToPos(rid);										
								result = fetchResultsFromPosition(cf, position);
								returnResult.add(result);
							}
						}								
						break;
					case '<':
						if(entry.key instanceof IntegerKey)	{
							if(((IntegerKey)entry.key).getKey() < hikeyInt){
								rid=((btree.LeafData)entry.data).getData();
								int position=reqHFile.RidToPos(rid);										
								result = fetchResultsFromPosition(cf, position);
								returnResult.add(result);
							}
						}
						else {
							if(((StringKey)entry.key).getKey().compareTo(highKeyStr)<0){
								rid=((btree.LeafData)entry.data).getData();
								int position=reqHFile.RidToPos(rid);										
								result = fetchResultsFromPosition(cf, position);
								returnResult.add(result);
							}
						}
						break;

					case '=':
						if(entry.key instanceof IntegerKey) {
							if(((IntegerKey)entry.key).getKey() == lowkeyInt){
								rid=((btree.LeafData)entry.data).getData();
								int position=reqHFile.RidToPos(rid);										
								result = fetchResultsFromPosition(cf, position);
								returnResult.add(result);
							}
						}
						else {
							if(((StringKey)entry.key).getKey().compareTo(lowkeyStr)==0){
								rid=((btree.LeafData)entry.data).getData();
								int position=reqHFile.RidToPos(rid);										
								result = fetchResultsFromPosition(cf, position);
								returnResult.add(result);
							}
						}
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
		return returnResult;
	}
	
	
	
	public List<List<String>> getResultsofBitmap(ColumnarFile cf,List<String> valueConstraint, String accessType) {

		List<List<String>> returnResult = new ArrayList<>();
		//List<String> result = new ArrayList<String>();
		
		return returnResult;
	}

	
	public void createBtreeIndex(ColumnarFile cf, String columnDbName, 
			String columnarFileName, String columnName) {

		BTreeFile bTreeFile = null; 
		String[] columnNames;
		columnNames = cf.columnNames;
		cf.columnNames = columnNames;

		int colNo = cf.getColumnNumberFromColumname(columnName);
		int type = cf.attributeType[colNo].attrType;

		Heapfile hf = cf.getHeapfileForColumname(columnName);

		try {
			RID rid = new RID();
			Scan s = hf.openScan();
			Tuple tuple=null;
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
			e.printStackTrace();
		}
	}
	
	public static void createBitmapIndex(ColumnarFile cf, String columnDbName, 
			String columnarFileName, String columnName) {

		BitMapFile B = null; 

		String[] columnNames;
		String file_entry_name;

		ColumnarFileMetadata cfm = cf.getColumnarFileMetadata(columnarFileName+".hdr");
		columnNames = cfm.columnNames;
		cf.columnNames = columnNames;

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
						B = new BitMapFile(file_entry_name, cf, ColNo, val);
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
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			fin = new FileInputStream("/Users/sucharitharumesh/eclipse-workspace/DBMSI_3/cse510/Sampledata/"+table);
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
	
	
	public List<String> fetchResultsFromPosition(ColumnarFile cf, int position) throws InvalidSlotNumberException, HFException, HFDiskMgrException, HFBufMgrException, Exception {

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

public List<String> getConditionList(String conjunctCond) {
		
		List<String> condition = new ArrayList<String>();
		String columnName;
		String operator;
		String columnValue;
		
		columnName = conjunctCond.substring(0, 1);
		operator = conjunctCond.substring(1, 2);
		columnValue = conjunctCond.substring(2);
		condition.add(columnName);
		condition.add(operator);
		condition.add(columnValue);
		
		return condition;
	}

@Override
public Tuple get_next() throws IOException, JoinsException, IndexException, InvalidTupleSizeException,
		InvalidTypeException, PageNotReadException, TupleUtilsException, PredEvalException, SortException,
		LowMemException, UnknowAttrType, UnknownKeyTypeException, Exception {
	// TODO Auto-generated method stub
	return null;
}

@Override
public void close() throws IOException, JoinsException, SortException, IndexException {
	// TODO Auto-generated method stub
	
}
}
