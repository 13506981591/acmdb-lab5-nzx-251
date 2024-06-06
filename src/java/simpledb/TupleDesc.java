package simpledb;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /*
        TDitem is the element of  td, thus td is a array list of TDitems
     * 
     */
    CopyOnWriteArrayList<TDItem> tdItems;


    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // some code goes here
        if (this.tdItems == null)
        {
            return null;
        }
        return this.tdItems.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // some code goes here
        tdItems = new CopyOnWriteArrayList<>();
        for(int index = 0;index<typeAr.length;index++)
        {
            tdItems.add(new  TDItem(typeAr[index], fieldAr[index]));
        }

    }

    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here
        tdItems = new CopyOnWriteArrayList<>();
        
        for(int index = 0;index<typeAr.length;index++)
        {
            tdItems.add(new  TDItem(typeAr[index],null));
        }
    }

    public TupleDesc() {
        // just for convenience of creating  a class TupleDesc
        tdItems = new CopyOnWriteArrayList<>();
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        return this.tdItems.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        if(i<0 || i>=this.tdItems.size())
        {
            throw new NoSuchElementException("it is not a valid field reference");
        }
        return tdItems.get(i).fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
        if(i<0 || i>=this.tdItems.size())
        {
            throw new NoSuchElementException("it is not a valid field reference");
        }
        return tdItems.get(i).fieldType;

    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        if(name ==  null)
        {
            throw new NoSuchElementException("no field with a matching name is found");
        }
        String Valid_name_part;
        // if(name.contains("."))
        // {
        //     Valid_name_part = name.substring(name.indexOf(".")+1);
        // }
        // else
        // {
        //     Valid_name_part = name;
        // }
        Valid_name_part = name;
        for (int index =0;index < tdItems.size();index++)
        {
            if(Valid_name_part.equals(getFieldName(index)))
            {
                return index;
            }
        }
        throw new NoSuchElementException("no field with a matching name is found");
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        int size = 0;
        for (TDItem item : tdItems)
        {
            size += item.fieldType.getLen();
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
        if(td1 == null)
        {
            return td2;
        }
        if(td2 == null)
        {
            return td1;

        }
        TupleDesc merge_result = new TupleDesc();
        for (int index = 0;index <td1.numFields();index++)
        {
                merge_result.tdItems.add(td1.tdItems.get(index));
        }
        for (int index = 0;index <td2.numFields();index++)
        {
                merge_result.tdItems.add(td2.tdItems.get(index));
        }
        return merge_result;
    }

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they are the same size and if the n-th
     * type in this TupleDesc is equal to the n-th type in td.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */
    public boolean equals(Object o) {
        // some code goes here
        if (! (o instanceof  TupleDesc))
        {
            return false;
        }
        //just for debug
        TupleDesc copy = (TupleDesc) o;
        //first judge the size and the num of fields
        if (copy.getSize() != this.getSize() || copy.numFields()!=this.numFields())
        {
            return false;
        }
        for (int index= 0;index<this.tdItems.size();index++)
        {
            if(!this.getFieldType(index).equals(copy.getFieldType(index)))
            {
                return false;
            }
        }


        return true;
    }

    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        // throw new UnsupportedOperationException("unimplemented");
        int hash_code = 0;
        for (TDItem item:this.tdItems)
        {
            hash_code += item.toString().hashCode();
        }
        return hash_code;
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        // some code goes here
        StringBuffer td_string = new StringBuffer();
        for (int index=0;index<this.numFields();index++)
        {
            TDItem item = this.tdItems.get(index);
            if (index != this.numFields()-1)
            {
                td_string.append(item.fieldType.toString()).append("(").append(item.fieldName.toString()).append("),");
                continue;
            }
            td_string.append(item.fieldType.toString()).append("(").append(item.fieldName.toString()).append(")");
        }
        return  td_string.toString();
    }
}
