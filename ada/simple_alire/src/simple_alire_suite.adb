with AUnit.Simple_Test_Cases; use AUnit.Simple_Test_Cases;
with Simple_Alire.Test; use Simple_Alire.Test;

package body Simple_Alire_Suite is

   function Suite return Access_Test_Suite is
      Ret : constant Access_Test_Suite := new Test_Suite;
   begin
      Ret.Add_Test (Test_Case_Access'(new Simple_Alire.Test.Test));
      return Ret;
   end Suite;

end Simple_Alire_Suite;
