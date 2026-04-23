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

    public abstract void delete(FileSystem fs); //This will be implemented in the subclasses, and it will delete the node and all of its children (if it's a directory)
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

    public void rmChild(String name, FileSystem fs){ //Delete directory or file
        Node child = children.get(name);
        if(child == null){
            System.out.println("No such file or directory: " + name);
            return;
        }
        child.delete(fs); //call it's delete method
        children.remove(name); //Remove key from the TreeMap
    }

    public void delete(FileSystem fs){
        //Delete directory and all of its children
        for(Node child : children.values()){
            child.delete(fs);
        }
        children.clear(); //Clear this directory's treemap

        //We don't need to remove this directory from its parent, because the parent will handle that when it calls rmChild

    }
}

class FileNode extends Node{
    //FileNode is inside of Directory's TreeMap, and it'll act as a index block
    //Therefore FileNode will store a list of pointers (ints) representing the addresses of the records of the file

    //private int indexBlockNum;
    private ArrayList<Integer> recordPointers; //This will store the pointers to the records of the file

    // public FileNode(String fileName, int indexBlockNum, Directory parent){
    //     super(fileName, parent);
    //     this.indexBlockNum = indexBlockNum;
    //     this.recordPointers = new ArrayList<>();
    // }

    public FileNode(String fileName, Directory parent){
        super(fileName, parent);
        this.recordPointers = new ArrayList<>();
    }


    public void addRecordPointer(int pointer){
        recordPointers.add(pointer);
    }

    public void clearRecords(){
        recordPointers.clear();
    }

    public void delete(FileSystem fs){
        //Delete the records from the virtual disk
        for(int pointer : recordPointers){
            fs.getDisk().deleteRecord(pointer);
        }
        clearRecords();
    }

    public void addRecord(FileSystem fs, String data){
        int recordAddress = fs.getDisk().addRecord(data);
        if(recordAddress != -1){
            addRecordPointer(recordAddress);
        }
        else{
            System.out.println("Failed to add record to disk because it is full.");
        }
    }

    public void clearFileContent(FileSystem fs){
        for(int pointer : recordPointers){
            fs.getDisk().deleteRecord(pointer);
        }
        clearRecords();
    }

    public ArrayList<Integer> getRecordPointers(){
        return recordPointers;
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

    public VirtualDisk getDisk(){
        return disk;
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
        catch(java.io.FileNotFoundException e){
            //Will do this the first time it runs
            System.out.println("Building new file system...");
            return null;
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
        System.out.println("touch \"filename\" - Create a new file (has to be in cwd)");
        System.out.println("rm \"filename\" - Delete a file or directory");
        System.out.println("open [\"filename\"] - Open file \"filename\" for reading/writing");
        System.out.println("read - Read contents from the currently opened file");
        System.out.println("write - Write data to the currently opened file (will override existing contents)");
        System.out.println("ls - List the contents of the current directory");
        System.out.println("cd \"filepath\" - Change the current directory (relative or absolute)");
        System.out.println("mkdir \"dirname\" - Create a new directory");
        System.out.println("pwd - Print the current working directory");
        System.out.println("tree - Print the file system as a tree from the current directory");
        System.out.println("help - Show this help message");
        System.out.println("exit - Exit the program");
        System.out.println();
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
        System.out.println();
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

        String indent = "  ".repeat(depth); //.reapeat() repeats the string a # of times, which is based on current depth 
        if(node instanceof Directory){
            Directory dir = (Directory) node;
            if(depth == 0){ //If this is the root directory, we won't print the |-- part, just the name of the directory
                System.out.println(dir.getName());
            }
            else{
                System.out.println(indent + "|-- " + dir.getName());
            }
            for(Node child : dir.getChildren().values()){
                printTreeHelper(child, depth + 1); //Recursively print the children of this directory, and increase the depth by 1
            }
        }

        else if(node instanceof FileNode){
            System.out.println(indent + "|-- " + node.getName());
            return;
        }

    }

    public static Directory changeDir(String filepath, Directory cwd, FileSystem fs){
        Directory current = null;

        //Figure out starting point
        if(filepath.startsWith("/")){ //absolute
            current = fs.root;
            filepath = filepath.substring(1); //Remove the leading "/" so we can split the path correctly
        }
        else{ //relative
            current = cwd;
        }

        //Split the filepath into parts based on "/"
        String[] parts = filepath.split("/");
        for(String part : parts){
            if(part.equals("") || part.equals(".")){
                //skip because either empty or starting at cwd (relative path)
                continue;
            }
            else if(part.equals("..")){
                //move up to parent directory if possible and end function
                if(current.getParent() != null){
                    current = current.getParent();
                    cwd = current; //Update the working directory to the new directory
                    break;
                }
                else{
                    System.out.println("Already at root directory, can't move up!");
                    return cwd;
                }
            }
            else{
                //Check if part exists in current's children
                if(current.getChildren().containsKey(part)){
                    Node child = current.getChildren().get(part);
                    if(child instanceof Directory){
                        current = (Directory) child; //Move down to the child directory
                        cwd = current; //Update the working directory to the new directory
                    }
                    else if(child instanceof FileNode){
                        System.out.println(filepath + " is a file, not a directory!");
                    }
                    else{
                        System.out.println("Error: invalid path");
                    }
                }
                else{
                    System.out.println("No such directory: " + filepath);
                    return cwd;
                }
            }
        }
        return cwd;
    }


    // public static void rm(String filepath, String name, Directory cwd, FileSystem fs){
    //     //Check if filepath is valid (absolute or relative)

    //     Directory current = null;

    //     if(filepath.startsWith("/")){ //absolute
    //         current = fs.root;
    //         filepath = filepath.substring(1); //Remove the leading "/" so we can split the path correctly
    //     }
    //     else{ //relative
    //         current = cwd;
    //     }

    //     //logic to find the directory the file "name" is in
    //     String[] parts = filepath.split("/");
    //     for(String part : parts){
    //         if(part.equals("") || part.equals(".")){
    //             continue;
    //         }
    //         else if(part.equals("..")){
    //             if(current.getParent()!=null){
    //                 current = current.parent;
    //             }
    //         }

    //     }

    // }


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
        boolean fileOpen = false;
        FileNode openFileNode = null; //needs to be a node so we can mess with the records

        do{
            
            System.out.print(workingDirectory.getName() + ": ");
            String command = scan.next();

            switch(command){
                case "help":
                    VFS_Project.helpCommand();
                    System.out.println();
                    break;

                case "pwd":
                    printAbsolutePath(workingDirectory);
                    System.out.println();
                    break;
                
                case "tree":
                    printTree(workingDirectory);
                    System.out.println();
                    break;

                case "ls":
                    for(String name : workingDirectory.getChildren().keySet()){
                        System.out.println("- " + name);
                    }
                    System.out.println();
                    break;

                case "touch":
                    String filename = scan.next();
                    if(workingDirectory.getChildren().containsKey(filename)){
                        System.out.println("File or directory with name " + filename + " already exists!");
                        System.out.println();
                        break;
                    }
                    else{
                        FileNode newFile = new FileNode(filename, workingDirectory);
                        workingDirectory.addChild(newFile);
                        System.out.println("File created: " + filename);
                        System.out.println();
                        break; 
                    }

                case "mkdir":
                    String dirname = scan.next();
                    if(workingDirectory.getChildren().containsKey(dirname)){
                        System.out.println("Directory " + dirname + " already exists in this directory!");
                        System.out.println();
                        break;
                    }
                    else{
                        Directory newDir = new Directory(dirname, workingDirectory);
                        workingDirectory.addChild(newDir);
                        System.out.println("Directory created: " + dirname);
                        System.out.println();
                        break;
                    }

                
                case "cd":
                    String filepath = scan.next();      
                    Directory newDir = changeDir(filepath, workingDirectory, fs);
                    if(newDir != null){
                        workingDirectory = newDir;
                    }
                    System.out.println();
                    break;

                case "rm":
                    String fileName = scan.next();
                    //check if fileName is inside of cwd map
                    if(workingDirectory.getChildren().containsKey(fileName)){
                        workingDirectory.rmChild(fileName, fs);
                    }
                    else{
                        System.out.println("File " + fileName + " is not in current directory");
                    }
                    System.out.println();
                    break;


                case "open":
                    fileName = scan.next();
                    if(workingDirectory.getChildren().containsKey(fileName)){
                        if(fileOpen == false){
                            fileOpen = true;
                            openFileNode = (FileNode) workingDirectory.getChildren().get(fileName);
                            System.out.println("Opening file " + fileName);
                        }
                        else{
                            System.out.println("File " + openFileNode.getName() + " is already open! Close it first");
                        }
                    }
                    System.out.println();
                    break;

                case "read":
                    if(fileOpen == false){
                        System.out.println("No file open!");
                        System.out.println();
                        break;
                    }

                    //grab the pointers
                    ArrayList<Integer> recordPointers = openFileNode.getRecordPointers();

                    //use pointers to find record in fs/VD
                    for(int pointer : recordPointers){
                        String data = fs.getDisk().readRecord(pointer);
                        if(data != null){
                            System.out.println(data);
                        }
                    }
                    System.out.println();
                    break;

                case "write":
                    if(fileOpen == false){
                        System.out.println("No file open!");
                        System.out.println();
                        break;
                    }
                
                    System.out.println("Please write a line of data. The more tokens you write, the more storage it'll allocate:");
                    scan.nextLine(); //just in case we have a leftover newline
                    String userInput = scan.nextLine();
                    
                    //Delete old records (since we're overwriting), but don't call delete() because technically we don't want to delete the file itself
                    openFileNode.clearFileContent(fs);

                    String[] tokens = userInput.split(" ");
                    int BLOCK_SIZE = 5; //5 tokens per record (idea from one Braylon's files)

                    for(int i = 0; i < tokens.length; i += BLOCK_SIZE){
                        //String data = tokens[i]+" "+tokens[i+1]+" "+tokens[i+2]+" "+tokens[i+3]+" "+tokens[i+4];

                        StringBuilder sb = new StringBuilder();
                        for(int v = i; v < i+BLOCK_SIZE && v <tokens.length; v++){
                            sb.append(tokens[v]).append(" "); //add that space at the end of the token
                        }
                        String data = sb.toString().trim(); //get rid of the last trailing space w/ trim
                        openFileNode.addRecord(fs, data);
                    }

                    System.out.println();
                    break;
                
                case "close":
                    if(fileOpen == false){
                        System.out.println("No file is open!");
                        System.out.println();
                        break;
                    }

                    fileOpen = false;
                    openFileNode = null;
                    System.out.println();
                    break;

                case "status":
                    fs.getDisk().showStatus();
                    System.out.println();
                    break;

                case "exit":
                    System.out.println("Saving work...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid command. Type 'help' to see a list of commands.");
                    System.out.println();
                    continue;
            }

        } while(running);

        scan.close();
        //save the file system before exiting
        fs.save("filesystem.ser");
    }

}