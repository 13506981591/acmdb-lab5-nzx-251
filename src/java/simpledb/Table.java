package simpledb;

public class Table {
    // a Table is associated with DB
    private DbFile file;
    //a Table has a name
    private String Table_Name;
    //primary key
    private String pkey_field;

    public Table(DbFile file, String Table_Name,String pkey_field)
    {
        this.file = file;
        this.Table_Name =Table_Name;
        this.pkey_field = pkey_field;

    }   
    // the primary key may be null
    public Table(DbFile file, String Table_Name)
    {
        this.file = file;
        this.Table_Name =Table_Name;
        this.pkey_field = "";

    }   
    public DbFile getFile()
    {
        return this.file;
    }
    public String getPrimaryKey()
    {
        return this.pkey_field;
    }
    public String getName()
    {
        return this.Table_Name;
    }
}
