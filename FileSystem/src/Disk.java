    /*
    Class: Disk
    Final Project: Linux File System
    Team: Wizard of OS (James Pfleger, Yvana Higgens, Chelsea Ip, Jordan Lawson, Dana Chinn)
    Summer 2019

    Disk: This class represents the hard drive disk in a computer. It allocates blocks to Inodes
        if space is available and keeps track of what blocks are allocated and what blocks are free
     */

    import java.io.PrintWriter;
    import java.util.ArrayList;


    public class Disk {
      int blocksAvailable = 999; //blocks avaiable in the bitmap
      private ArrayList<Boolean> bitmap = new ArrayList<>(); // bitmap holding which blocks are available
      PrintWriter out; // our logging class


      // Constructor - sets all bits to false (formats the disk)
      public Disk()
      {
        for(int i = 0; i <= 1000; i++){
          bitmap.add(false);
        }
      }

      // For giving the class the log file to write to
      public void assignLog(PrintWriter newLog){
        out = newLog;
      }

      // returns true if there are enough blocks available to allocated
      public boolean isThereSpace(int blocksNeeded)
      {
        if (blocksAvailable < blocksNeeded)
        {
          out.println("No space available!");
          return false;
        }
        return true;
      }

      // allocates the requested amount of blocks to the inode and sets their validity bits to 1 (in use)
      public void allocateBlocks(Inode file, int blocks)
      {
        // checks there is enough space in the disk
        if(blocksAvailable > blocks)
        {
          blocksAvailable-= blocks; // decrement total blocks available
          out.print("Allocated Blocks: "); // write to log

          for(int i = 0; i < blocks; i++)
          {
            int blockIndex = bitmap.indexOf(false); // finds the first available block
            bitmap.set(blockIndex, true); // sets valid bit to allocated
            file.addDirectBlock(blockIndex); // adds block to inode directBlockArray
            out.print(blockIndex + " "); // updates log of allocated block
          }
          out.println();
          return;
        }
        out.println("No blocks available!");
      }

      // sets the valid bit to available for the given block index
      public boolean freeBlock(int blockIndex)
      {
        if(blockIndex < 1000 && blockIndex != -1)
        {
          blocksAvailable++; // increments total blocks available
          bitmap.set(blockIndex, false); // sets validity bit back to 0 (available)
          out.print(blockIndex + " "); // updates log of freed block
          return true;
        }
        return false;
      }

      // prints bitmap to log
      public void flushToOutput()
      {
        out.println("Final Bitmap: ");
        String output = "";

        // prints 20 x 50 bitmap
        for(int i = 0; i < 50; i++)
        {
            for (int j = 0; j < 20; j++)
            {
                int index = (i * 20) + j;
                Boolean bit = bitmap.get(index);
                if (bit) {
                    output += index + ": 1  ";
                } else
                    output += index + ": 0  ";
            }
            output += "\n";
        }
        out.println(output);
      }
    }
