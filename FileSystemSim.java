import java.nio.ByteBuffer;
import java.util.Scanner;
import java.io.RandomAccessFile;

class VFSConfig 
{
    //example for the VFS we don't have to use these measures
    public static final int BLOCK_SIZE = 1024;
    public static final int MAX_INODES = 64;
    public static final int INODE_SIZE = 128;
    public static final int INODE_TABLE_OFFSET = BLOCK_SIZE * 3;
}

class Inode 
{
    public int isDirectory; // 1 = Dir, 0 = File
    public int fileSize;
    //pointers 
    public int[] directPointers = new int[10];
   
    //
    public byte[] toBytes() 
    {
        ByteBuffer buffer = ByteBuffer.allocate(VFSConfig.INODE_SIZE);
        buffer.putInt(isDirectory);
        buffer.putInt(fileSize);
        for (int ptr : directPointers) buffer.putInt(ptr);
        return buffer.array();
    }
}

public class FileSystemSim 
{
    // keeps track of where the users are in the system
    private static String currentPath = "/";
    private static int currentInodeIndex = 0; 

    public static void main(String[] args) 
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("VFS Initialized. Root Inode: " + currentInodeIndex);

        while (true) {
            // Updated dynamic prompt: shows the path before the '>'
            System.out.print(currentPath + " > ");
            
            String inputLine = scanner.nextLine();
            if (inputLine.isEmpty()) continue;
            
            String[] input = inputLine.split(" ");
            String command = input[0];

            try {
                switch (command) 
                {
                    case "ls":
                        listDirectory();
                        break;
                    
                    case "cd":
                        if(input.length > 1) 
                        {
                            changeDirectory(input[1]);
                        }
                        break;
                    
                    case "create":
                        System.out.println("Creating file in Inode " + currentInodeIndex + "...");
                        break;
                    
                    case "exit":
                        System.out.println("Closing Virtual Disk...");
                        return;
                    
                    default:
                        System.out.println("Unknown command: " + command);
                }
            } 
            
            catch (Exception e) 
            {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Placeholder for listing files (Logical mapping)
    private static void listDirectory() {
        System.out.println("Contents of " + currentPath + ":");
        System.out.println(". (dir)\n.. (dir)");
    }

    // Placeholder for moving between folders
    private static void changeDirectory(String target) {
        if (target.equals("..")) {
            currentPath = "/"; // Simplified for now
            currentInodeIndex = 0;
        } else {
            currentPath += target + "/";
            // In a real system, you'd find the Inode of 'target' here
            // currentInodeIndex = foundInode; 
        }
    }
}