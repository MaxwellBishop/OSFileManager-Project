import java.util.HashMap;

public abstract class Node{
    String name; //This will be the name of the node (filename or directory)
    Directory Parent; //This will be the parent directory of this node
}

class Directory extends Node{
    //The directory will store a HashMap of all of the index blocks (files)
    private HashMap<String, Integer> fileMap;
    
    public Directory(HashMap<String, Integer> fileMap){
        this.fileMap = fileMap;
    }

    public void addtoHash(){
        
    }

}