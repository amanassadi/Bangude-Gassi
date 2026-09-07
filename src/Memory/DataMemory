package simulator;

public class DataMemory {
   private int[] memory = new int[256];

   public DataMemory() {
   }

   private void checkAddress(int var1) {
      if (var1 < 0 || var1 > 255) {
         throw new IllegalArgumentException("Invalid memory address");
      }
   }

   public void write(int var1, int var2) {
      this.checkAddress(var1);
      this.memory[var1] = var2 & 255;
   }

   public int read(int var1) {
      this.checkAddress(var1);
      return this.memory[var1];
   }

   public void reset() {
      for(int var1 = 0; var1 < this.memory.length; ++var1) {
         this.memory[var1] = 0;
      }

   }
}
