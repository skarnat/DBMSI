package joins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bitmap.BitMapFile;
import btree.BTFileScan;
import btree.BTreeFile;
import btree.IntegerKey;
import btree.KeyDataEntry;
import btree.StringKey;
import columnar.ColumnarFile;
import columnar.ColumnarFileMetadata;
import columnar.TupleScan;
import global.AttrType;
import global.Convert;
import global.RID;
import global.TID;
import heap.HFBufMgrException;
import heap.HFDiskMgrException;
import heap.HFException;
import heap.Heapfile;
import heap.InvalidSlotNumberException;
import heap.Scan;
import heap.Tuple;
import value.IntegerValue;
import value.StringValue;

public class ColumnarNestedLoopsJoins {

	String outerFile;
	String innerFile;
	String columnDB;
	
	public ColumnarNestedLoopsJoins(String columnDB, String outerFile, String innerFile) {
		this.columnDB = columnDB;
		this.outerFile = outerFile;
		this.innerFile = innerFile;
	}

	public List<List<String>> getResults(ColumnarFile outerCf, ColumnarFile innerCf, 
			String outerConstraint, String innerConstraint, String joinConstraint, 
			String outerAccessType, String innerAccessType,
			List<String> targetColumnNames, int numOfBuffers) {
		
		List<List<String>> innerResults = new ArrayList<>();
		List<List<String>> outerResults = new ArrayList<>();
		
		outerConstraint = outerConstraint.replaceAll("\\[","").replaceAll("\\]","");
		innerConstraint = innerConstraint.replaceAll("\\[","").replaceAll("\\]","");
		joinConstraint = joinConstraint.replaceAll("\\[","").replaceAll("\\]","");
		
		List<String> outputFilter = new ArrayList<String>();
		List<String> innerFilter = new ArrayList<String>();
		
		/*
		columnName = innerConstraint.substring(0, 1);
		operator = innerConstraint.substring(1, 2);
		columnValue = innerConstraint.substring(2);
		innerFilter.add(columnName);
		innerFilter.add(operator);
		innerFilter.add(columnValue);
		*/
		String columnName = this.outerFile+"."+joinConstraint.substring(0, 1);
		String operator = joinConstraint.substring(1, 2);
		String columnValue = this.innerFile+"."+joinConstraint.substring(2, 3);
		outputFilter.add(columnName);
		outputFilter.add(operator);
		outputFilter.add(columnValue);
		
		/*
		List<String> outputFilterAndConditions = new ArrayList<String>();
		
		String[] outerTableConditions = outerConstraint.split("&");
		for(String outerTableCondition : outerTableConditions) {
			if(!outerTableCondition.contains("|")){
				outputFilterAndConditions.add(outerTableCondition);
				System.out.println(outerTableCondition);
			} else{
				
			}
		}
		*/
		List<List<String>> temp = new ArrayList<>();
		List<String> outerTableConditions = new ArrayList<>();		
		if(outerConstraint.contains("&")){
			outerTableConditions = Arrays.asList(outerConstraint.split("&"));
		} else {
			outerTableConditions.add(outerConstraint);
		}
		List<String> innerTableConditions = new ArrayList<>();		
		if(innerConstraint.contains("&")){
			innerTableConditions = Arrays.asList(innerConstraint.split("&"));
		} else {
			innerTableConditions.add(innerConstraint);
		}

		switch (outerAccessType) {
			case "FILESCAN":				
				for(String outerTableCondition : outerTableConditions) {
					List<String> outerAndFilter = new ArrayList<String>();
					if(!outerTableCondition.contains("|")){
						outerAndFilter = getConditionList(outerTableCondition);
						temp = getResultsFileScan(outerCf, targetColumnNames, outerAndFilter, numOfBuffers);
						if(outerResults.size() == 0)
							outerResults.addAll(temp);
						else
							outerResults.retainAll(temp);
						//System.out.println(outerResults.toString());
					} else{
						outerTableCondition = outerTableCondition.replaceAll("\\(","").replaceAll("\\)","");
						String[] outerTableOrConditions = outerTableCondition.split("\\|");
						for(String outerTableOrCondition : outerTableOrConditions) {
							List<String> outerOrFilter = getConditionList(outerTableOrCondition);
							temp = getResultsFileScan(outerCf, targetColumnNames, outerOrFilter, numOfBuffers);
							outerResults.addAll(temp);
						}
						Set<List<String>> hs = new HashSet<>();
						hs.addAll(outerResults);
						outerResults.clear();
						outerResults.addAll(hs);
						//System.out.println(outerResults.toString());
					}
				}
				System.out.println("Outer Table Records using FILESCAN");
				System.out.println(outerResults.toString());
				break;
			case "BTREE":
				for(String outerTableCondition : outerTableConditions) {
					List<String> outerAndFilter = new ArrayList<String>();
					if(!outerTableCondition.contains("|")){
						outerAndFilter = getConditionList(outerTableCondition);
						temp = getResultsBtree(outerCf, targetColumnNames, outerAndFilter, numOfBuffers, "outer");
						if(outerResults.size() == 0)
							outerResults.addAll(temp);
						else
							outerResults.retainAll(temp);
						//System.out.println(outerResults.toString());
					} else{
						outerTableCondition = outerTableCondition.replaceAll("\\(","").replaceAll("\\)","");
						String[] outerTableOrConditions = outerTableCondition.split("\\|");
						for(String outerTableOrCondition : outerTableOrConditions) {
							List<String> outerOrFilter = getConditionList(outerTableOrCondition);
							temp = getResultsBtree(outerCf, targetColumnNames, outerOrFilter, numOfBuffers, "outer");
							outerResults.addAll(temp);
						}
						Set<List<String>> hs = new HashSet<>();
						hs.addAll(outerResults);
						outerResults.clear();
						outerResults.addAll(hs);
						//System.out.println(outerResults.toString());
					}
				}
				System.out.println("Outer Table Records using BTREE");
				System.out.println(outerResults.toString());
				break;
			case "BITMAP":
				//innerResults = getResultsBitmap(innerCf, targetColumnNames, outerFilter, outerAccessType, numOfBuffers, "inner");
				break;
			default:
				break;
		}
		
		switch (innerAccessType) {
			case "FILESCAN":
				for(String innerTableCondition : innerTableConditions) {
					List<String> innerAndFilter = new ArrayList<String>();
					if(!innerTableCondition.contains("|")){
						innerAndFilter = getConditionList(innerTableCondition);
						temp = getResultsFileScan(innerCf, targetColumnNames, innerAndFilter, numOfBuffers);
						if(innerResults.size() == 0)
							innerResults.addAll(temp);
						else
							innerResults.retainAll(temp);
						//System.out.println(innerResults.toString());
					} else{
						innerTableCondition = innerTableCondition.replaceAll("\\(","").replaceAll("\\)","");
						String[] innerTableOrConditions = innerTableCondition.split("\\|");
						for(String innerTableOrCondition : innerTableOrConditions) {
							List<String> innerOrFilter = getConditionList(innerTableOrCondition);
							temp = getResultsFileScan(innerCf, targetColumnNames, innerOrFilter, numOfBuffers);
							innerResults.addAll(temp);
						}
						Set<List<String>> hs = new HashSet<>();
						hs.addAll(innerResults);
						innerResults.clear();
						innerResults.addAll(hs);
						//System.out.println(innerResults.toString());
					}
				}
				System.out.println("Inner Table Records using FILESCAN");
				System.out.println(innerResults.toString());
				//innerFilter = getConditionList(innerConstraint);
				//innerResults = getResultsFileScan(innerCf, targetColumnNames, innerFilter, numOfBuffers);
				break;
			case "BTREE":
				for(String innerTableCondition : innerTableConditions) {
					List<String> innerAndFilter = new ArrayList<String>();
					if(!innerTableCondition.contains("|")){
						innerAndFilter = getConditionList(innerTableCondition);
						temp = getResultsBtree(innerCf, targetColumnNames, innerAndFilter, numOfBuffers, "inner");
						if(innerResults.size() == 0)
							innerResults.addAll(temp);
						else
							innerResults.retainAll(temp);
						//System.out.println(innerResults.toString());
					} else{
						innerTableCondition = innerTableCondition.replaceAll("\\(","").replaceAll("\\)","");
						String[] innerTableOrConditions = innerTableCondition.split("\\|");
						for(String innerTableOrCondition : innerTableOrConditions) {
							List<String> innerOrFilter = getConditionList(innerTableOrCondition);
							temp = getResultsBtree(innerCf, targetColumnNames, innerOrFilter, numOfBuffers, "inner");
							innerResults.addAll(temp);
						}
						Set<List<String>> hs = new HashSet<>();
						hs.addAll(innerResults);
						innerResults.clear();
						innerResults.addAll(hs);
						//System.out.println(innerResults.toString());
					}
				}
				System.out.println("Inner Table Records using BTREE");
				System.out.println(innerResults.toString());
				//innerFilter = getConditionList(innerConstraint);
				//innerResults = getResultsBtree(innerCf, targetColumnNames, innerFilter, numOfBuffers, "inner");
				break;
			case "BITMAP":
				innerResults = getResultsBitmap(innerCf, targetColumnNames, innerFilter, numOfBuffers, "inner");
				break;
			default:
				break;
		}

		System.out.println(outputFilter);
		String outerJoinColumn = outputFilter.get(0);
		int outerJoinColumnNumber = getColumnNumber(outerJoinColumn.charAt(outerJoinColumn.length() - 1)+"");
		//String joinOperator = outputFilter.get(1);
		String innerJoinColumn = outputFilter.get(2);
		int innerJoinColumnNumber = getColumnNumber(innerJoinColumn.charAt(innerJoinColumn.length() - 1)+"");

		List<List<String>> result = new ArrayList<>();

		for(List<String> outerResult : outerResults) {	
			for(List<String> innerResult : innerResults) {
				List<String> res = new ArrayList<>();
				if(outerResult.get(outerJoinColumnNumber).equals(innerResult.get(innerJoinColumnNumber))) {
					for (String targetColumn : targetColumnNames) {
						int columnNumber = getColumnNumber(targetColumn);
						res.add(outerResult.get(columnNumber));
						res.add(innerResult.get(columnNumber));
					}
				}
				if (res.size() > 0) {
					result.add(res);
				}
			}
		}
		return result;
	}

	public List<String> getConditionList(String tableCondition) {
		
		List<String> condition = new ArrayList<String>();
		String columnName;
		String operator;
		String columnValue;
		
		columnName = tableCondition.substring(0, 1);
		operator = tableCondition.substring(1, 2);
		columnValue = tableCondition.substring(2);
		condition.add(columnName);
		condition.add(operator);
		condition.add(columnValue);
		
		return condition;
	}

	public static int getColumnNumber(String columnName){
		int column = 1;
		switch(columnName){
		case "A":
			column = 0;
			break;
		case "B":
			column = 1;
			break;
		case "C":
			column = 2;
			break;
		case "D":
			column = 3;
			break;
		}
		return column;
	}

	public List<List<String>> getResultsFileScan(ColumnarFile cf, List<String> targetColumnNames, List<String> valueConstraint, int numBuf) {

		List<List<String>> returnResult = new ArrayList<>();
		List<String> result = new ArrayList<String>();

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
			int lowkeyInt=0;
			String lowkeyStr="";
			if(type == 1) {						
				lowkeyInt= Integer.parseInt(valueConstraint.get(2));
			} else {
				lowkeyStr = valueConstraint.get(2);
			}

			char operator = valueConstraint.get(1).charAt(0);

			AttrType[] columnType = cf.attributeType;
			while ((t=ts.getNext(tid))!=null) {
				if (type == 1) {
					switch(operator) {
					case '>':
						if ( lowkeyInt < (t.getIntFld(columnNumber+1))) {
							result = t.getRecord(columnType);
							returnResult.add(result);
						}
						break;
					case '<':
						if (lowkeyInt > (t.getIntFld(columnNumber+1))) {
							result = t.getRecord(columnType);
							returnResult.add(result);
						}
						break;
					case '!':
						if (!(lowkeyInt == (t.getIntFld(columnNumber+1)))) {
							result = t.getRecord(columnType);
							returnResult.add(result);
						}
						break;
					case '=':
						if (lowkeyInt == (t.getIntFld(columnNumber+1))) {
							result = t.getRecord(columnType);
							returnResult.add(result);
						}
						break;

					}
				} else {
					switch(operator) {
					case '>':
						if ((lowkeyStr.compareTo(t.getStrFld(columnNumber+1)) < 0)) {
							result = t.getRecord(columnType);
							returnResult.add(result);
						}
						break;
					case '<':
						if ((lowkeyStr.compareTo(t.getStrFld(columnNumber+1)) > 0)) {
							result = t.getRecord(columnType);
							returnResult.add(result);
						}
						break;
					case '!':
						if (!(lowkeyStr.compareTo(t.getStrFld(columnNumber+1)) == 0)) {
							result = t.getRecord(columnType);
							returnResult.add(result);
						}
						break;
					case '=':
						if ((lowkeyStr.compareTo(t.getStrFld(columnNumber+1)) == 0)) {
							result = t.getRecord(columnType);
							returnResult.add(result);
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

	public List<List<String>> getResultsBtree(ColumnarFile cf, List<String> targetColumnNames, List<String> valueConstraint, int numBuf, String columnarFileName) {

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
				createBtreeIndex(cf, this.columnDB, columnarFileName, indexColumn);
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
				createBtreeIndex(cf, this.columnDB, columnarFileName, indexColumn);
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

	public List<List<String>> getResultsBitmap(ColumnarFile cf, List<String> targetColumnNames, List<String> valueConstraint, int numBuf, String columnarFileName) {

		List<List<String>> returnResult = new ArrayList<>();
		//List<String> result = new ArrayList<String>();
		
		return returnResult;
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


}