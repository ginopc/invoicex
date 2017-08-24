intPID = %PID%
strComputer = "."
set objWMIProcess = GetObject("winmgmts:\\" & strComputer & "\root\cimv2:Win32_Process.Handle='" & intPID & "'")
intRC = objWMIProcess.Terminate()
if intRC = 0 Then
   Wscript.Echo "0"
else
   Wscript.Echo "Error code: " & intRC & " Process name: " & objWMIProcess.Name
end if
Wscript.Echo "exit"