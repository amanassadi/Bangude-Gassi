package simulator;

public class StackMemory {
   private DataMemory memory;
   private int sp = 7;

   public StackMemory(DataMemory var1) {
      this.memory = var1;
   }

   public void push(int var1) {
      if (this.sp >= 255) {
         throw new IllegalStateException("Stack overflow");
      } else {
         ++this.sp;
         this.memory.write(this.sp, var1);
      }
   }

   public int pop() {
      if (this.sp <= 7) {
         throw new IllegalStateException("Stack underflow");
      } else {
         int var1 = this.memory.read(this.sp);
         --this.sp;
         return var1;
      }
   }

   public int getSP() {
      return this.sp;
   }

   public void reset() {
      this.sp = 7;
   }
}
