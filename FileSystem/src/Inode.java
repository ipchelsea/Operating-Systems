/*
 Class: Inode
 Final Project: Linux File System
 Team: Wizard of OS (James Pfleger, Yvana Higgens, Chelsea Ip, Jordan Lawson, Dana Chinn)
 Summer 2019

 Inode: Represents the inodes in the Linus file system. Able to assign up to 10 blocks of memory
    per Inode.
  */

import java.io.PrintWriter;
import java.util.ArrayList;

public class Inode {
  //each inode contains
    private String accessMode;
    private String userId;
    private String groupId;
    private int memSize = 0;
    private ArrayList<Integer> directBlocks = new ArrayList<>();
    PrintWriter out;


    public Inode(int blockCount, Disk disc){
      accessMode = "-rwxr--r--";
      userId = "CSS430";
      groupId = "CSS430";
    }

    // For giving the class the log file to write to
    public void assignLog(PrintWriter newLog){
      out = newLog;
    }


    // removes the amount blocks specified from the directBlocksArray
    // assumes that if all blocks are deleted, inode should be deleted
    // starts with latest allocated block
    public int deleteBlocks(Disk disc, int blocks){
      // check if number of blocks being deallocated is larger than blocks allocated
      if(blocks > directBlocks.size()){
        out.println("Deleting more blocks than in the file!");
        deleteFile(disc); // deletes the entire inode
        return 1;
      }

      // delete blocks from the back end of the direct block array one at a time
      // 0 = success, 1 = delete file instead -1 = failure in deallocating memory
      out.print("Freed block(s): ");
      for(int i = 0; i < blocks; i++){
        int delBlock = directBlocks.remove(directBlocks.size()-1);

        // free from the disc
        if(!disc.freeBlock(delBlock)){
          return -1;
        }
        //clean up memory in directBlock
        else{
          // -1 represents null
        }
      }
      out.println();
      return 0;
    }

    // returns true if direct blocks are deleted, returns false if memory deallocation fails
    public boolean deleteFile(Disk disc)
    {
      out.print("Freed block(s): ");
      while(directBlocks.size() > 0)
      {
        int delBlock = directBlocks.remove(0);

        if(!disc.freeBlock(delBlock))
        {
          //out.println("Failed");
          return false;
        }
      }
      out.println();
      return true;
    }

    // checks that there are enough direct block pointers to add more blocks to file
    public boolean directBlockAvailable(int blocksNeeded)
    {
      if(blocksNeeded <= (10 - directBlocks.size()))
      {
        return true;
      }
      return false;
    }

    // returns the amount of directBlocks allocated to inode
    public int getDirectBlocks()
    {
      return directBlocks.size();
    }

    // allocates blocks to the inode. Either adds all blocks requested or none if
    //  there isn't enough space on file or space on disk
    public void addBlocks(int additionalBlocks, Disk disc)
    {
      if(!(directBlockAvailable(additionalBlocks)))
      {
        out.println("Not enough blocks available!");
        return;
      }

      disc.allocateBlocks(this, additionalBlocks); // disk allocates blocks and flips validity bits
      memSize = directBlocks.size() * 512;
    }

    // assigns a block to the inode
    // no error handling
    public void addDirectBlock(int blockIndex)
    {
      directBlocks.add(blockIndex);
    }

    // toString function to print inode to log
    public String toString()
    {
      String temp = "Access Mode: " + accessMode + "\n";
      temp += "User ID: " + userId + " Group ID: " + groupId + "\n";
      temp += "Size: " + directBlocks.size() * 512 + " bytes\n" + "Direct Blocks:\n";
      temp += directBlocks.toString() + "\n";

      return temp;
    }

}
