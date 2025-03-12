with AUnit.Assertions; use AUnit.Assertions;

package body Simple_Alire.Test is

   overriding function Name (T : Test) return AUnit.Message_String is 
       pragma Unreferenced(T); 
   begin
      return AUnit.Format ("Test Simple Alire package");
   end Name;

   overriding procedure Run_Test (T : in out Test) is
      pragma Unreferenced (T);
   begin
       Assert(False, "Failed something");
   end Run_Test;
end Simple_Alire.Test;
