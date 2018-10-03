package columnar;

import global.AttrType;
import global.TupleOrder;

public class ColumnarSort {
	
	AttrType[] in;
	short len_in;
	short[] str_sizes;
	ColumnarFile cname;
	int sort_fld;
	TupleOrder sort_order;
	int sort_fld_len;
	int n_page;
	
	ColumnarSort(
			AttrType[] in2,
			short len_in2,
			short[] str_sizes2,
			ColumnarFile cname2,
			int sort_fld2,
			TupleOrder sort_order2,
			int sort_fld_len2,
			int n_page2 ){
		
		this.in = in2;
		this.len_in = len_in2;
		this.str_sizes = str_sizes2;
		this.cname = cname2;
		this.sort_fld = sort_fld2;
		this.sort_order = sort_order2;
		this.sort_fld_len = sort_fld_len2;
		this.n_page = n_page2;
		
		
		
	}

}
