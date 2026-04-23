import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Scanner;
import java.io.*; // added for FileWriter, File, and IOException

abstract class Node 
{
    protected String name;
    protected Directory parent;

    public Node(String name, Directory parent) 
    {
        this.name = name;
        this.parent = parent;
    }

    public String getName() 
    {
        return name;
    }

    public Directory getParent() 
    {
        return parent;
    }

    public abstract void delete();
}

class Directory extends Node 
{
    private TreeMap<String, Node> children = new TreeMap<>();

    public Directory(String name, Directory parent) {
        super(name, parent);
    }

    public void addChild(Node child) 
    {
        children.put(child.getName(), child);
        child.parent = this;
    }

    public TreeMap<String, Node> getChildren() 
    {
        return children;
    }

    public void rmChild(String name) 
    {
        Node child = children.get(name);
        if (child == null) 
        {
            System.out.println("No such file or directory: " + name);
            return;
        }
        child.delete();
        children.remove(name);
    }

    public void delete() 
    {
        for (Node child : children.values()) 
        {
            child.delete();
        }
        children.clear();
    }

    // ---------------------------------------------------------
    // listFiles function for ls 
    // ---------------------------------------------------------
    public void listFiles() 
    {
        if (children.isEmpty()) 
        {
            System.out.println("(Directory is empty)");
            return;
        }
        // ---------------------------------------------------------
        // iterates and displays all stored nodes
        // ---------------------------------------------------------
        for (String key : children.keySet()) 
        {
            Node n = children.get(key);
            String type = (n instanceof Directory) ? "[DIR] " : "[FILE]";
            System.out.println(type + " " + key);
        }
        // ---------------------------------------------------------
    }
}

class FileNode extends Node 
{
    int indexBlockNum;
    ArrayList<Integer> recordPointers;

    public FileNode(String fileName, int indexBlockNum, Directory parent) 
    {
        super(fileName, parent);
        this.indexBlockNum = indexBlockNum;
        this.recordPointers = new ArrayList<>();
    }

    public void addRecordPointer(int pointer) 
    {
        recordPointers.add(pointer);
    }

    public void clearRecords() 
    {
        recordPointers.clear();
    }

    public void delete() 
    {
        clearRecords();
    }
}

class Record 
{
    private String data;
    private int recordAddress;

    public Record(int recordAddress, String data) 
    {
        this.recordAddress = recordAddress;
        this.data = data;
    }

    public String getData() 
    {
        return data;
    }

    public int getRecordAddress() 
    {
        return recordAddress;
    }

    public void setData(String newData) 
    {
        this.data = newData;
    }
}

class VirtualDisk 
{
    //had to initialize the disk list so no NullPointerException when addRecord is called
    private ArrayList<Record> disk = new ArrayList<>(); 
    private ArrayList<Integer> freeBlocks = new ArrayList<>();

    public VirtualDisk() 
    {
        for (int i = 0; i < 100; i++) 
        {
            freeBlocks.add(i);
        }
    }

    public void readRecord(int address) 
    {
        for (Record record : disk) {
            if (record.getRecordAddress() == address) 
            {
                System.out.println(record.getData());
                return;
            }
        }
        System.out.println("No such record with address: " + address);
    }

    public void addRecord(String data) 
    {
        if (freeBlocks.isEmpty()) 
        { //just changed it to isEmpty cause i thought it looked a bit cleaner 
            System.out.println("Disk is full!");
            return;
        }
        int address = freeBlocks.remove(0);
        Record record = new Record(address, data);
        disk.add(record);
    }

    public void deleteRecord(int address) 
    {
        Record toRemove = null; //avoiding modifying list while iterating or lead to ConcurrentModificationException
        for (Record record : disk) {
            if (record.getRecordAddress() == address) 
            {
                toRemove = record;
                break;
            }
        }
        if (toRemove != null) 
        {
            disk.remove(toRemove);
            freeBlocks.add(address);
        }
    }

    public void showStatus() 
    {
        System.out.println("Virtual Disk Status:");
        System.out.println("Used Blocks: " + disk.size());
        System.out.println("Free Blocks: " + freeBlocks.size());
    }
}

public class BrayVersionFileSystem 
{
    public static void main(String[] args) 
    {
        Scanner scan = new Scanner(System.in);

        // ---------------------------------------------------------
        // initialized for tree hieachry 
        // ---------------------------------------------------------
        Directory root = new Directory("root", null);
        Directory currentDir = root;
        VirtualDisk vDisk = new VirtualDisk();
        // ---------------------------------------------------------

        String command;
        String newName;
        String content;
        
        // specified java.io.File to stop collision with FileNode or other classes
        java.io.File openFile = null; 
        String currentDirName = "root";

        boolean enderMan = true;

        while (enderMan) 
        {
            //  shows the path to current folder
            System.out.print("EliteHacker@/" + currentDir.getName() + "--> ");
            
            // checks if there is input available so no NoSuchElementException
            if(!scan.hasNext()) break; 
            
            command = scan.next();

            switch (command) 
            {
                case "create":
                    boolean checker = false;
                    System.out.print("Enter file name: ");
                    newName = scan.next();
                    
                    // checks current directory children
                    if(currentDir.getChildren().containsKey(newName)) {
                        System.out.println("Error: Name already exists in this directory.");
                        break;
                    }

                    try {
                        FileWriter myWriter = new FileWriter(newName);
                        scan.nextLine(); //takes away the new line from scan.next()
                        System.out.print("File contents: ");
                        content = scan.nextLine();
                        myWriter.write(content);
                        myWriter.close();
                        
                        // links new file to current Directory tree
                        FileNode fn = new FileNode(newName, 0, currentDir);
                        currentDir.addChild(fn);
                        vDisk.addRecord(content);

                        checker = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (checker) System.out.println("File Created");
                    break;

                case "mkdir":
                    // ---------------------------------------------------------
                    // creates new directory node within current folder
                    // ---------------------------------------------------------
                    System.out.print("Enter directory name: ");
                    newName = scan.next();
                    if(currentDir.getChildren().containsKey(newName)) 
                    {
                        System.out.println("Error: Name already exists.");
                    } 
                    else 
                    {
                        Directory newDir = new Directory(newName, currentDir);
                        currentDir.addChild(newDir);
                        System.out.println("Directory " + newName + " created in /" + currentDir.getName());
                    }
                    // ---------------------------------------------------------
                    break;

                case "ls":
                    // ---------------------------------------------------------
                    // ls function
                    // ---------------------------------------------------------
                    currentDir.listFiles();
                    // ---------------------------------------------------------
                    break;

                case "cd":
                    // ---------------------------------------------------------
                    // chnages dir and files (supports ".." for parent or folder names)
                    // ---------------------------------------------------------
                    System.out.print("change directory to: "); // Keeping original text string
                    newName = scan.next();
                    if (newName.equals("..")) {
                        if (currentDir.getParent() != null) {
                            currentDir = currentDir.getParent();
                        } else {
                            System.out.println("Already at root.");
                        }
                    } else {
                        Node target = currentDir.getChildren().get(newName);
                        if (target instanceof Directory) {
                            currentDir = (Directory) target;
                        } else {
                            System.out.println("Directory not found.");
                        }
                    }
                    // ---------------------------------------------------------
                    break;

                case "rmdir":
                case "delete":
                    // ---------------------------------------------------------
                    // removes files or  directories no matter if empty
                    // ---------------------------------------------------------
                    System.out.print("Enter name to remove: ");
                    newName = scan.next();
                    Node toDelete = currentDir.getChildren().get(newName);
                    if (toDelete != null) {
                        if (toDelete instanceof FileNode) {
                            java.io.File f = new java.io.File(newName);
                            f.delete(); // physical delete
                        }
                        currentDir.rmChild(newName); // virtual delete
                        System.out.println("Item deleted"); 
                    } else {
                        System.out.println("No such file or directory.");
                    }
                    // ---------------------------------------------------------
                    break;

                case "close":
                    openFile = null;
                    System.out.println("File closed");
                    break;

                case "open":
                    System.out.print("Select a file: ");
                    newName = scan.next();
                    
                    // verify file exists in current directory before opening
                    Node fileNode = currentDir.getChildren().get(newName);
                    if (fileNode instanceof FileNode) {
                        //uses java.io.File to check physical disk existence
                        java.io.File temp = new java.io.File(newName); 
                        if (temp.exists()) {
                            openFile = temp;
                            System.out.println(newName + " Opened");
                        }
                    } else {
                        System.out.println("File does not exist!");
                    }
                    break;

                case "read":
                    if (openFile != null) {
                        try {
                            Scanner reader = new Scanner(openFile);
                            while (reader.hasNextLine()) {
                                System.out.println(reader.nextLine());
                            }
                            reader.close();
                        } catch (IOException e) {
                            System.out.println("File not found.");
                        }
                    } else {
                        System.out.println("No file opened!");
                    }
                    break;

                case "write":
                    if (openFile != null) {
                        try {
                            //added 'true' parameter to FileWriter for appending mode
                            FileWriter myWriter = new FileWriter(openFile, true); 
                            scan.nextLine(); // clear buffer
                            System.out.print("File contents: ");
                            content = scan.nextLine();
                            myWriter.write(content);
                            myWriter.close();
                            System.out.println("Wrote to File");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("No file open D:<");
                    }
                    break;

                case "shutdown":
                    System.out.println("Shutting Down >:)");
                    enderMan = false;
                    break;

                default:
                    System.out.println("No such command exists");
            }
        }
        scan.close(); // closes resources
    }
}