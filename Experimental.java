import java.util.*;

public class Experimental{

/*public void setCommand(String command_in)
      {
         command = command_in;  //user sets command
      }*/

public static void main(String[] args)
   {
   
      Scanner scan = new Scanner(System.in);
      String command;
      
      
      boolean enderMan = true;
      
      while(enderMan!=false){
      
      System.out.print("EliteHacker69--> ");
      command = scan.next();
      
      switch(command)
         {
            case "create": //create
            {
               System.out.println("File Created");
            }
            break;
            case "delete": //delete
            {
               //myLine.dequeue();
               System.out.println("File deleted");
               
            }
            break;
            case "open": //open
            {
               //System.out.println(myLine);
               System.out.println("File Opened");
            }
            break;
            case "read": //read
            {
               System.out.println("File Read");

            }
            break;
            case "write": //write
            {
               System.out.println("Wrote to File");

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
               enderMan = false;
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