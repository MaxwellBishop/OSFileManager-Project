import java.util.*;
import java.io.FileWriter;   
import java.io.IOException;
import java.io.File;

public class Experimental{

/*public void setCommand(String command_in)
      {
         command = command_in;  //user sets command
      }*/

public static void main(String[] args)
   {
   
      Scanner scan = new Scanner(System.in);
      String command;
      String newName;
      String content;
      //String openFile;  //may want to change the openFile to its own object type?
      File openFile = null;
      String directory = "root";  //again, change to its own object type
      
      boolean enderMan = true;
      
      while(enderMan!=false){
      
      System.out.print("EliteHacker9/18/47--> ");
      command = scan.next();
      
      switch(command)
         {
            case "create": //create new file
            {
             boolean chekcer = false;
             System.out.print("Enter file name: ");//name new file
             newName = scan.next();
               try {
                     FileWriter myWriter = new FileWriter(newName);
                     scan.nextLine();
                     System.out.print("File contents: "); //write to the new file
                     content = scan.nextLine();
                     myWriter.write(content);
                     chekcer = true;
                     myWriter.close();
                   } catch (IOException e) {
                     e.printStackTrace();
               }
               if(chekcer==true){
                  System.out.println("File Created");//execute once created
               }
            }
            break;
            case "delete": //delete
            {
               
               System.out.println("File deleted");
               
            }
            break;
            case "close": //close
            {
               
               openFile = null;
               System.out.println("File closed");
               
            }
            break;
            case "open": //open
            {
               System.out.print("Select a file: ");
               newName = scan.next();
               
               File temp = new File(newName);
               if(temp.exists()){
                  openFile = temp; //set which file is opened
                  System.out.println(newName+" Opened");
               }
               else{
                  System.out.println("File does not exist!");
               }
            }
            break;
            case "read": //read
            {
               if(openFile!=null){
                  try{
                     Scanner reader = new Scanner(openFile);
                     while (reader.hasNextLine()) {
                         System.out.println(reader.nextLine()); //print out the file for user to read
                     }
                     reader.close();
                  }catch (IOException e) {
                        System.out.println("File not found.");
                  }
               }
               else
               {
                  System.out.println("No file opened!");
               }
               
               //System.out.println("File Read");

            }
            break;
            case "write": //write
            {
               if(openFile!=null){
                  try {
                        /*System.out.print("Name of file: ");
                        scan.nextLine();
                        newName = scan.nextLine();*/
                        FileWriter myWriter = new FileWriter(openFile, true);
                        scan.nextLine();
                        System.out.print("File contents: "); //write to the new file
                        content = scan.nextLine();
                        myWriter.write(content);
                        myWriter.close();
                      } catch (IOException e) {
                        e.printStackTrace();
                  }
                  System.out.println("Wrote to File");
               }
               else
               {
                  System.out.println("No file open D:<");
               }
            }
            break;
            case "shutdown": //shutdown
            {
               System.out.println("Shutting Down >:)");
               enderMan = false;
            }
            break;
            case "cd": //change directory
            {
               System.out.println("change directory to: ");
               
            }
            break;
            default:
            {
               System.out.println("No such command exists");
            }
            
         }
         
         }//while loop end
         
   
   }//end main




}//end file