import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Scanner;


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

abstract class Node implements Serializable{
    protected String name; //This will be the name of the node (filename or directory)
    protected Directory parent; //This will be the parent directory of this node

    //Protected because we don't want to allow direct access to these variables, 
    //but we want to allow access in the subclasses (Directory and FileNode)

    //Add permissions (if there's time)

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
    //FileNode is inside of Directory's TreeMap, and it'll act as a index block
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

    public void delete(VirtualDisk disk){
        //Delete the records from the virtual disk
        for(int pointer : recordPointers){
            disk.deleteRecord(pointer);
        }
        delete(); //Delete the file node itself
    }

    public void delete(){
        clearRecords();
    }

    public void addRecord(VirtualDisk disk, String data){
        int recordAddress = disk.addRecord(data);
        if(recordAddress != -1){
            addRecordPointer(recordAddress);
        }
        else{
            System.out.println("Failed to add record to disk because it is full.");
        }
    }
}

class Record implements Serializable{
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

    public Record getRecord(){
        return this;
    }

}


class VirtualDisk implements Serializable{
    //VirtualDisk will manage storage of records (allocate, free, read, write)

    private Map<Integer, Record> disk = new HashMap<>(); //This will represent the virtual disk, and it will store the records

    private ArrayList<Integer> freeBlocks; //Number of free blocks

    public VirtualDisk(){
        this.freeBlocks = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            freeBlocks.add(i); //Initialize the free blocks with addresses 0-99
        }
    }

    public String readRecord(int address){
        Record record = disk.get(address);

        if(record == null){
            System.out.println("No record found at address: " + address);
            return null;
        }
        return record.getData();
    }

    public int addRecord(String data){
        if(freeBlocks.size() == 0){
            System.out.println("Disk is full! Remove some files to free up space.");
            return -1;
        }
        int address = freeBlocks.remove(0); //Get the address of the first free block
        Record record = new Record(address, data);
        //add the record to the virtual disk
        disk.put(address, record);
        return address;
    }

    public void deleteRecord(int address){
        //Find the record with the given address and remove it from the disk
        if(disk.containsKey(address)){
            disk.remove(address);
            freeBlocks.add(address); //Add the address back to the free blocks
            return;
        }
        System.out.println("No such record with address: " + address);
    }

    public void writeRecord(int address, String newData){
        //Find the record with the given address and update its data
        if(disk.containsKey(address)){
            Record record = disk.get(address);
            record.setData(newData);
            return;
        }
        
        System.out.println("No such record with address: " + address);
    }

    public void showStatus(){
        System.out.println("Virtual Disk Status:");
        System.out.println("Total Blocks: " + disk.size());
        System.out.println("Free Blocks: " + freeBlocks.size());
    }

}


class User implements Serializable{

    private String username;
    private Directory homeDirectory;

    public User(String username, Directory homeDirectory){
        this.username = username;
        this.homeDirectory = homeDirectory;
    }

    public String getUsername(){
        return username;
    }

    public Directory getHomeDirectory(){
        return homeDirectory;
    } 
    

}


class FileSystem implements Serializable{ //Manages FS and makes it so we can save the info so VFS doesn't get wiped every time we run the program 
    Directory root;
    VirtualDisk disk;
    Map<String, User> users; //This will store the users of the file system, and the key will be the username

    public FileSystem(){
        this.root = new Directory("root", null); //Root directory has no parent
        this.disk = new VirtualDisk();
        this.users = new HashMap<>();

        //Add the /home directory to root
        Directory home = new Directory("home", root);
        root.addChild(home);
    }

    public Directory getHomeDir(){
        return (Directory) root.getChildren().get("home");
    }

    public void save(String filename){
        //This will save the file system to a file using serialization
        try{
            java.io.FileOutputStream fileOut = new java.io.FileOutputStream(filename);
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(fileOut);
            out.writeObject(this);
            out.close();
            fileOut.close();
            //System.out.println("File system saved to " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static FileSystem load(String filename){
        try{
            java.io.FileInputStream fileIn = new java.io.FileInputStream(filename);
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(fileIn);
            FileSystem fs = (FileSystem) in.readObject();
            in.close();
            fileIn.close();
            return fs;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}




public class VFS_Project {
    
    public static void helpCommand(){
        System.out.println("status - Show the status of the virtual disk");
        System.out.println("touch \"filename\" - Create a new file");
        System.out.println("rm \"filename\" - Delete a file or directory");
        System.out.println("open [\"filename\"] - Open file \"filename\" for reading/writing");
        System.out.println("read - Read contents from the currently opened file");
        System.out.println("read [\"filename\"] - Reads contents of specified file");
        System.out.println("write - Write data to the currently opened file");
        System.out.println("write [\"filename\"]- Write to filename (will overwrite existing contents)");
        System.out.println("ls - List the contents of the current directory");
        System.out.println("cd \"dirname\" - Change the current directory");
        System.out.println("mkdir \"dirname\" - Create a new directory");
        System.out.println("pwd - Print the current working directory");
        System.out.println("tree - Print the file system as a tree from the current directory");
        System.out.println("help - Show this help message");
        System.out.println("exit - Exit the program");
        System.out.println(); System.out.println();
    }


    public static void printAbsolutePath(Directory cwd){
        ArrayList<String> path = new ArrayList<>();
        Directory current = cwd;

        //Traverse up the directory tree until we reach the root, adding each directory name to the path list

        while(current != null){
            path.add(current.getName()); //Add the directory name to arraylist
            current = current.getParent(); //Move up to the parent directory
        }

        for(int i = path.size() - 1; i >= 0; i--){
            System.out.print("/" + path.get(i)); //Print the path in reverse order (from home->cwd)
        }
    }

    public static void printTree(Directory cwd){ //DFS traversal
        printTreeHelper(cwd, 0);
    }

    public static void printTreeHelper(Node node, int depth){
        /*Example output: (dir1 is cwd)
            dir1
            |-- file1
            |-- file2
            |-- dir2
                |-- file3
        */

        /*Example output 2:
            file1
        */

        if(node instanceof Directory){}

        else if(node instanceof FileNode){
            System.out.println(node.getName());
            return;
        }

    }

    public static void main(String[] args) {
        //This is where we will implement the CLI and the virtual disk
        //Implemented code from Experiemental.java

        //See if the file system already exists, if it does, load it, if it doesn't, create a new one
        FileSystem fs;
        try{
            fs = FileSystem.load("filesystem.ser");
        }
        catch(Exception e){
            fs = null;
        }
        if(fs == null){
            fs = new FileSystem();
        }

        
        //Initial setup:

        Scanner scan = new Scanner(System.in);
        String username;
        System.out.print("Enter your username: ");
        username = scan.next();

        //If username doesn't exist, create a new user with a root directory
        if(!fs.users.containsKey(username)){
            Directory homeDir = new Directory(username, fs.getHomeDir());
            fs.getHomeDir().addChild(homeDir); //add it to the treemap (the big one we'll print as a tree)
            User newUser = new User(username, homeDir);
            fs.users.put(username, newUser);
        }

        System.out.println("Welcome to the Virtual File System " + username + "!\n");
        System.out.println("Type 'help' to see a list of commands.\n");

        boolean running = true;
        Directory workingDirectory = fs.users.get(username).getHomeDirectory(); //initilize working directory to user's home directory

        do{
            
            System.out.print(workingDirectory.getName() + ": ");
            String command = scan.next();

            switch(command){
                case "help":
                    VFS_Project.helpCommand();
                    break;

                case "pwd":
                    printAbsolutePath(workingDirectory);
                    break;
                
                case "tree":
                    printTree(workingDirectory);
                    break;






                

                case "exit":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid command. Type 'help' to see a list of commands.");
                    continue;
            }

        } while(running);

        scan.close();
        //save the file system before exiting
        fs.save("filesystem.ser");
    }

}