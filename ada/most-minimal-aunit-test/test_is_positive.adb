with Ada.Integer_Text_IO; use Ada.Integer_Text_IO;
with Ada.Text_IO;         use Ada.Text_IO;
with Ada.Assertions;      use Ada.Assertions;
with Is_Positive;

procedure Test_Check_Positive is

   --function Is_Positive(Number : Integer) return Boolean;

begin
   -- Test positive number
   Assert(Is_Positive(5));

   -- Test zero
   Assert(Is_Positive(0) = False);
   
   -- Test negative number
   Assert(Is_Positive(-3) = False);
   Assert(Is_Positive(-3));
end Test_Check_Positive;
