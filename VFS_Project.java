import java.util.ArrayList;
import java.util.TreeMap;


/*
Plans for project:
- Indexed Allocation
- Data structures we'll use:
  - Tree (for the directory tree)
    - Index block points to each record, therefore index block will store a list of pointers
    - There's one index block / file
- Connecting the Index blocks together:
  - We can just use a hashmap/table so we can look up the files by filename
  - There will be ONE hashmap / directory
  - We could even use a linked list to make it simple too idc

- Printing as a tree:
  - We'll print the tree based off the directory files, and their children
  - Directory file is an Index block that stores address to hashmap (or linkedlist)

- Example hiarchy in java (from chat):

abstract class Node {
    String name;
    Directory parent; // key for hierarchy
}

class Directory extends Node {
    Map<String, Node> children = new HashMap<>();
}

class FileNode extends Node {
    int indexBlockPointer;
    String fileName;
}



Goal of the project:
- Create a file system that uses indexed allocation to store files and directories
- Implement a way to print the file system as a tree:
Directory
 ├── FileNode
 ├── FileNode
 └── Directory
      ├── FileNode
      └── Directory


A cool idea of a better output:
[D] root
  [F] file1 (3 records)
  [D] subdir
    [F] file2 (5 records)

I think including the records part would be cool!
    
- Implement CLI commands to create, delete, open, read, and write files/directories in the file system

- Implement a virtual disk to store the files and directories
*/



//Node class is for building the tree

abstract class Node{
    protected String name; //This will be the name of the node (filename or directory)
    protected Directory parent; //This will be the parent directory of this node

    //Protected because we don't want to allow direct access to these variables, 
    //but we want to allow access in the subclasses (Directory and FileNode)

    public Node(String name, Directory parent){
        this.name = name;
        this.parent = parent;
    }

    public String getName(){
        return name;
    }

    public Directory getParent(){
        return parent;
    }

    public abstract void delete(); //This will be implemented in the subclasses, and it will delete the node and all of its children (if it's a directory)
}

class Directory extends Node{

    private TreeMap<String, Node> children = new TreeMap<>(); //TreeMap gives you auto alphabetical ordering (and it won't be random unlike hashmap)
    //children will store either an indexblock (file) or another directory, and the key will be the name of the file/directory

    public Directory(String name, Directory parent){ 
        super(name, parent);
    }

    public void addChild(Node child){ //Create directory or file
        children.put(child.getName(), child);
        child.parent = this; //Set the parent of the child to this directory
    }

    public Directory getParent(){
        return parent;
    }

    //Not used (but might be useful for the CLI)
    public TreeMap<String, Node> getChildren(){
        return children;
    }

    public void rmChild(String name){ //Delete directory or file
        Node child = children.get(name);
        if(child == null){
            System.out.println("No such file or directory: " + name);
            return;
        }
        child.delete(); //call it's delete method
        children.remove(name); //Remove key from the TreeMap
    }

    public void delete(){
        //Delete directory and all of its children
        for(Node child : children.values()){
            child.delete();
        }
        children.clear(); //Clear this directory's treemap

        //We don't need to remove this directory from its parent, because the parent will handle that when it calls rmChild

    }
}

class FileNode extends Node{
    //FileNode is inside of Directory's HashMap, and it'll act as a index block
    //Therefore FileNode will store a list of pointers (ints) representing the addresses of the records of the file

    int indexBlockNum;
    ArrayList<Integer> recordPointers; //This will store the pointers to the records of the file

    public FileNode(String fileName, int indexBlockNum, Directory parent){
        super(fileName, parent);
        this.indexBlockNum = indexBlockNum;
        this.recordPointers = new ArrayList<>();
    }


    public void addRecordPointer(int pointer){
        recordPointers.add(pointer);
    }

    public void clearRecords(){
        recordPointers.clear();
    }

    public void delete(){
        clearRecords(); //Get rid of the recordpointers (which will free up the records in the virtual disk)
    }
}

class Record{
    //Record stores the data of the file, and you can reference it using the index block in FileNode

    private String data;
    private int recordAddress; //This will be the address of the record in memory (or on disk)

    public Record(int recordAddress, String data){
        this.recordAddress = recordAddress;
        this.data = data;
    }

    public String getData(){
        return data;
    }

    public int getRecordAddress(){
        return recordAddress;
    }

    public void setData(String newData) {
        this.data = newData;
    }

}


class VirtualDisk{
    //VirtualDisk will manage storage of records (allocate, free, read, write)

    private ArrayList<Record> disk; //This will represent the virtual disk, and it will store the records

    private ArrayList<Integer> freeBlocks; //Number of free blocks

    public VirtualDisk(){
        this.freeBlocks = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            freeBlocks.add(i); //Initialize the free blocks with addresses 0-99
        }
    }

    public void readRecord(int address){
        //Find the record with the given address and print its data
        for(Record record : disk){
            if(record.getRecordAddress() == address){
                System.out.println(record.getData());
                return;
            }
        }
        System.out.println("No such record with address: " + address);
    }

    public void addRecord(String data){
        if(freeBlocks.size() == 0){
            System.out.println("Disk is full! Remove some files to free up space.");
            return;
        }
        int address = freeBlocks.remove(0); //Get the address of the first free block
        Record record = new Record(address, data);
        //add the record to the virtual disk
        disk.add(record);
    }

    public void deleteRecord(int address){
        //Find the record with the given address and remove it from the disk
        for(Record record : disk){
            if(record.getRecordAddress() == address){
                disk.remove(record);
                freeBlocks.add(address); //Add the address back to the free blocks (no need to sort imo)
                return;
            }
        }
        System.out.println("No such record with address: " + address);
    }

    public void writeRecord(int address, String newData){
        //Find the record with the given address and update its data
        for(Record record : disk){
            if(record.getRecordAddress() == address){
                record.setData(newData);
                return;
            }
        }
        System.out.println("No such record with address: " + address);
    }

    public void showStatus(){
        System.out.println("Virtual Disk Status:");
        System.out.println("Total Blocks: " + disk.size());
        System.out.println("Free Blocks: " + freeBlocks.size());
    }

}



public class VFS_Project {
    public static void main(String[] args) {
        //This is where we will implement the CLI and the virtual disk
        System.out.println("Hello!");
        //This is a comment to test the commit for braylon
        //Im thinking we can use a switch and scanner
    }
}