import java.util.ArrayList;
import java.util.HashMap;


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
    
- Implement CLI commands to create, delete, open, read, and write files/directories in the file system
*/



//Node class is for building the tree

public abstract class Node{
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
}

class Directory extends Node{

    private HashMap<String, Node> children = new HashMap<>();
    //children will store either an indexblock (file) or another directory, and the key will be the name of the file/directory

    public Directory(String name, Directory parent, HashMap<String, Node> children){
        super(name, parent);
        this.children = children;
    }
}

class FileNode extends Node{
    //FileNode is inside of Directory's HashMap, and it'll act as a index block
    //Therefore FileNode will store a list of pointers (ints) representing the addresses of the records of the file

    String fileName; int indexBlockNum;
    ArrayList<Integer> recordPointers; //This will store the pointers to the records of the file

    public FileNode(String fileName, int indexBlockNum){
        super(fileName, null); //FileNode doesn't technically have a parent because it's stored in the Directory's HashMap
        this.fileName = fileName;
        this.indexBlockNum = indexBlockNum;
        this.recordPointers = new ArrayList<>();
    }


    public void addRecordPointer(int pointer){
        recordPointers.add(pointer);
    }
}

class Record{
    //Record stores the data of the file, and you can reference it using the index block in FileNode

    String data;
    int recordAddress; //This will be the address of the record in memory (or on disk)

    public Record(String data){
        this.data = data;
    }



}
