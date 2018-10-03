package iterator;
import java.lang.*;
import java.io.*;
import global.*;

/**
 *  This clas will hold single select condition
 *  It is an element of linked list which is logically
 *  connected by OR operators.
 */

public class CondExpr {
  
  /**
   * Operator like "<"
   */
  public AttrOperator op;    
  
  /**
   * Types of operands, Null AttrType means that operand is not a
   * literal but an attribute name
   */    
  public AttrType     type1;
  public AttrType     type2;    
 
  /**
   *the left operand and right operand 
   */ 
  public Operand operand1;
  public Operand operand2;
  
  /**
   * Pointer to the next element in linked list
   */    
  public CondExpr    next;   
  
  /**
   *constructor
   */
  public  CondExpr() {
    
    operand1 = new Operand();
    operand2 = new Operand();
    
    operand1.integer = 0;
    operand2.integer = 0;
    
    next = null;
  }
  public CondExpr(int typ1, int typ2,String colName, String value,int oper) {
	  operand1 = new Operand();
	  operand2 = new Operand();
	  operand1.string = colName;
	  operand2.string = value;
	  
	  type1 =  new AttrType(typ1);
	  type2 = new AttrType(typ2);
	 
	  
	  op = new AttrOperator(oper);
	  next = null;
	  
  }
  public CondExpr(int typ1, int typ2,String colName, Integer value,int oper) {
	  operand1 = new Operand();
	  operand2 = new Operand();
	  operand1.string = colName;
	  operand2.integer = value;
	  
	  type1 =  new AttrType(typ1);
	  type2 = new AttrType(typ2);
	 
	  op = new AttrOperator(oper);
	  next = null;
	  
  }
  public void set_next(CondExpr val) {
	  next = val;
	  
  }
}
//bmj outer inner [A] [(C<3|D>7)&A<Delaware] [D<7]  [A=A] 2
//bmj outer inner [A,B] [B=Delaware] [A=Connecticut] [D=C] 2
//bmj outer inner [A,B] [B=Delaware&C<7] [A=Connecticut&D>2] [D=C] 2
//bmj cdb outer inner [A] [C<3] [D<7] [A=A] 2
//[()and()and()]
		
