with AUnit.Reporter.Text;
with AUnit.Run;
with Simple_Alire_Suite; use Simple_Alire_Suite;

procedure Testrunner is
   procedure Runner is new AUnit.Run.Test_Runner (Suite);
   Reporter : AUnit.Reporter.Text.Text_Reporter;
begin
   Runner (Reporter);
end Testrunner;
