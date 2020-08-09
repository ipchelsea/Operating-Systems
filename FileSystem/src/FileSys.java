/*
Holds the inode
Holds Disk
Receives commands from public static void main(String[] args) {
holds the directory

Assignment 4: James, Jordan, Yvana, Dana & Chelsea
*/

import java.io.PrintWriter;
import java.util.*;


public class FileSys {

  private HashMap<String, Integer> inodeDirectory = new HashMap<>();
  private Inode[] inodeArray = new Inode[100]; // number of inodes allowed is size of this array
  private Disk disc = new Disk();
  PrintWriter out;

  public FileSys() {
  }

  // For giving the class the log file to write to
  public void assignLog(PrintWriter newLog){
    out = newLog;
    disc.assignLog(out);
  }

  public boolean createFile(String name, int blocks)
  {
    // check if name exists in directory
    if(checkIfInodeExist(name) == -1)
    {
      // check if enough blocks are free from Disk
      if (disc.isThereSpace(blocks))
      {
        int n = nextAvailableInodeSlot(); // check for available inode index
        Inode ind = new Inode(blocks, disc); // create inode and insert into inode Array
        out.println("Assigning file: " + name + " inode ID: " + n);
        ind.assignLog(out);
        ind.addBlocks(blocks, disc); // allocates blocks to inode


        inodeArray[n] = ind; // adds inode to inodeArray

        inodeDirectory.put(name, n); // insert name and inode number into directory
        return true;
      }
    }
    return false;
  }

  // adds blocks to inode if space is available on inode and on disk
  public boolean addBlocks(String name, int blocks)
  {
    int inodeIndex = checkIfInodeExist(name); // checks if the file exists
    if (inodeIndex == -1)
    {
      out.println("File: " + name + " doesn't exist, cannot add blocks to it.");
      return false;
    }
    if(!(disc.isThereSpace(blocks))) // checks if there is space on disk
    {
      out.println("Not enough space on disk to add " + blocks + " to file: " + name);
      return false;
    }
    Inode temp = inodeArray[inodeIndex];

    if(!(temp.directBlockAvailable(blocks))) // checks if there is space on inode
    {
      out.println("File: " + name + " too large, cannot add " + blocks);
      return false;
    }

    temp.addBlocks(blocks, disc); // allocates the additional blocks
    return true;
  }

  // deallocates blocks from the file
  public boolean deleteBlocks(String name, int blocks)
  {
    int ID;
    Inode file;
    // get inode number from directory
    // if name not there -> file doesn't exist
    ID = checkIfInodeExist(name);
    // If Inode exists then we proceed
    if(ID != -1)
    {
      file = inodeArray[ID];
      // get inode from inode Array
      // tell disc to flip bits to available
      if(file.getDirectBlocks() <= blocks)
      {
        out.println("Deleting more blocks than file holds: deleting file: " + name);
        deleteFile(name);
      }
      else
      {
        inodeArray[ID].deleteBlocks(disc, blocks);
      }
      return true;
    }
    else
    {
      out.println("Error: File not found");
    }
    return false;
  }

  // deallocates all blocks from inode and then deletes inode from inodeArray and inodeDirectory
  public boolean deleteFile(String name)
  {
    int ID;
    Inode file;

    ID = checkIfInodeExist(name);

    if(ID != -1)
    {
      // get inode from inode Array
      file = inodeArray[ID];

      // tell disc to flip bits to available (all from inode direct ref array)
      // delete inode from inode array and set index to null
      // delete name and inode number (map entry) from directory
      inodeArray[ID] = null;
      inodeDirectory.remove(name);
      return file.deleteFile(disc);

    }


    out.println("Error: File not found");
    return false;
  }

  // checks the inodeDirectory for a file name
  public int checkIfInodeExist(String name)
  {
    if(inodeDirectory.get(name) == null)
    {
      return -1;
    }
    else
    {
      return inodeDirectory.get(name);
    }
  }

  public void closeFile()
  {
    out.close();
  }

  // finds the next available inode index
  public int nextAvailableInodeSlot()
  {
    int index = -1;
    // for the size of the array check if one of them are null
    for(int i = 0; i < inodeArray.length; i++)
    {
      // if there is an empty space return the index
      if(inodeArray[i] == null)
      {
        return i;
      }
    }
    // else return the value of next index
    return index;
  }



  // printing methods for end of program
  public void printDisk(){
    disc.flushToOutput();
  }

  public void printInode()
  {
    for (String name: inodeDirectory.keySet())
    {
      String fileName = name;
      int inodeNumber = inodeDirectory.get(name);
      String output = "*********************************\n";
      output += "File Name: " + fileName + "\nInode ID: " + inodeNumber +"\n";
      output += inodeArray[inodeNumber].toString();
      output += "*********************************";

      out.println(output);
    }
  }

}
