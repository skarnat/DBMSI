/*
 * @(#) BT.java   98/05/14
 * Copyright (c) 1998 UW.  All Rights Reserved.
 *         Author: Xiaohu Li (xioahu@cs.wisc.edu)
 *
 */
package bitmap;

import java.io.*;
import java.lang.*;
import global.*;
import diskmgr.*;
import bufmgr.*;
import heap.*;

/**  
 * This file contains, among some debug utilities, the interface to our
 * key and data abstraction.  The BTLeafPage and BTIndexPage code
 * know very little about the various key types.  
 *
 * Essentially we provide a way for those classes to package and 
 * unpackage <key,data> pairs in a space-efficient manner.  That is, the 
 * packaged result contains the key followed immediately by the data value; 
 * No padding bytes are used (not even for alignment). 
 *
 * Furthermore, the BT<*>Page classes need
 * not know anything about the possible AttrType values, since all 
 * their processing of <key,data> pairs is done by the functions 
 * declared here.
 *
 * In addition to packaging/unpacking of <key,value> pairs, we 
 * provide a keyCompare function for the (obvious) purpose of
 * comparing keys of arbitrary type (as long as they are defined 
 * here).
 *
 * For debug utilities, we provide some methods to print any page
 * or the whole B+ tree structure or all leaf pages.
 *
 */
public class BM  implements GlobalConst{
  //Default Constructor
	public BM() {
		
		
	}
	
	/** For debug. Print Bitmap structure out
	   *@param header  the head page of the Bitmapfile
	   *@exception IOException error from the lower layer
	   *@exception ConstructPageException  error from BT page constructor
	   *@exception IteratorException  error from iterator
	   *@exception HashEntryNotFoundException  error from lower layer
	   *@exception InvalidFrameNumberException  error from lower layer
	   *@exception PageUnpinnedException  error from lower layer
	   *@exception ReplacerException  error from lower layer
	   */
	  public static void printBitMap(BitMapHeaderPage header) 
	    throws IOException, 
		   ConstructPageException, 
		   IteratorException,
		   HashEntryNotFoundException,
		   InvalidFrameNumberException,
		   PageUnpinnedException,
		   ReplacerException 
	    {
	      if(header.get_rootId().pid == INVALID_PAGE) {
		System.out.println("The BitMap is Empty!!!");
		return;
	      }
	      
	      System.out.println("");
	      System.out.println("");
	      System.out.println("");
	      System.out.println("---------------BitMap---------------");
	      
	      
	      System.out.println(1+ "     "+header.get_rootId());
	      
	      _printTree(header.get_rootId());
	      
	      System.out.println("--------------- End ---------------");
	      System.out.println("");
	      System.out.println("");
	    }

	  private static void _printTree(PageId currentPageId) 
		throws IOException, 
	  {

			BMPage page = new BMPage(currentPageId);
			PageId pageid = page.getCurPage();
			page.openBMpage(page);
			byte [] data;
		    data = page.getBMpageArray();
		    System.out.println(data);
		    
		    PageId apage = page.getNextPage();
		    if(apage.pid != INVALID_PAGE) {
		    _printTree(apage);
		    
		    }
		    
	  
	  }

  } // end of BM





