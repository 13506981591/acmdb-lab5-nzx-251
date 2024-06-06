package simpledb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Knows how to compute some aggregate over a set of IntFields.
 */
public class IntegerAggregator implements Aggregator {

    private static final long serialVersionUID = 1L;
    // which column used for grouping
    private int gbfield;
    // which column used for aggregate
    private int afiled;
    private Type gbfieldtype;

    private Op aggreOp;
    private TupleDesc tupleDesc;

    private Map<Field, Integer> GroupByValue;
    private Map<Field, Integer> Counts;

    /**
     * Aggregate constructor
     * 
     * @param gbfield
     *                    the 0-based index of the group-by field in the tuple, or
     *                    NO_GROUPING if there is no grouping
     * @param gbfieldtype
     *                    the type of the group by field (e.g., Type.INT_TYPE), or
     *                    null
     *                    if there is no grouping
     * @param afield
     *                    the 0-based index of the aggregate field in the tuple
     * @param what
     *                    the aggregation operator
     */

    public IntegerAggregator(int gbfield, Type gbfieldtype, int afield, Op what) {
        // some code goes here
        this.gbfield = gbfield;
        this.gbfieldtype = gbfieldtype;
        this.afiled = afield;
        this.aggreOp = what;

        if (gbfield == Aggregator.NO_GROUPING)
            this.tupleDesc = new TupleDesc(new Type[] { Type.INT_TYPE }, new String[] { "aggregateValue" });
        else
            this.tupleDesc = new TupleDesc(new Type[] { gbfieldtype, Type.INT_TYPE },
                    new String[] { "groupValue", "aggregateValue" });
        GroupByValue = new HashMap<>();
        Counts = new HashMap<>();
    }

    /**
     * Merge a new tuple into the aggregate, grouping as indicated in the
     * constructor
     * 
     * @param tup
     *            the Tuple containing an aggregate field and a group-by field
     */
    public void mergeTupleIntoGroup(Tuple tup) {
        // some code goes here
        Field groupfield;
        if (gbfield == Aggregator.NO_GROUPING)
            groupfield = null;
        else
            groupfield = tup.getField(gbfield);

        Integer oldvalue = GroupByValue.get(groupfield);
        Integer nowvalue = ((IntField) tup.getField(afiled)).getValue();
        Integer newvalue = null;

        switch (aggreOp){
            case MIN:
                {
                    if (oldvalue == null) newvalue = nowvalue;
                    else newvalue = Integer.min(oldvalue, nowvalue);
                    break;
                }
            case MAX:
                {
                    if (oldvalue == null) newvalue = nowvalue;
                    else newvalue = Integer.max(oldvalue, nowvalue);
                    break;
                }
            case COUNT:
                {
                    if (oldvalue == null) newvalue = 1;
                    else newvalue = oldvalue + 1;
                    break;
                }
            case SUM:
                {
                    if (oldvalue == null) newvalue = nowvalue;
                    else newvalue = nowvalue + oldvalue;
                    break;
                }
            case AVG:
                {
                    if (oldvalue == null) newvalue = nowvalue;
                    else newvalue = nowvalue + oldvalue;

                    Integer cnt = Counts.getOrDefault(groupfield, 0);
                    Counts.put(groupfield, cnt + 1);

                    break;
                }
        }

        GroupByValue.put(groupfield, newvalue);
    }

    /**
     * Create a DbIterator over group aggregate results.
     * 
     * @return a DbIterator whose tuples are the pair (groupVal, aggregateVal)
     *         if using group, or a single (aggregateVal) if no grouping. The
     *         aggregateVal is determined by the type of aggregate specified in
     *         the constructor.
     */
    public DbIterator iterator() {
        // some code goes here
    //     throw new UnsupportedOperationException("please implement me for lab3");
    // }
    ArrayList <Tuple> tuples = new ArrayList<>();
    for (Map.Entry<Field, Integer> entry : GroupByValue.entrySet())
    {
        Tuple tuple = new Tuple(tupleDesc);
        Integer value = entry.getValue();
        if (aggreOp == Op.AVG) 
        {
            value = value / Counts.get(entry.getKey());
        }

        if (gbfield == Aggregator.NO_GROUPING)
        {
            tuple.setField(0, new IntField(value));
        } 
        else 
        {
            tuple.setField(0, entry.getKey());
            tuple.setField(1, new IntField(value));
        }
        tuples.add(tuple);
    }
    return new TupleIterator(tupleDesc, tuples);
    }

}
